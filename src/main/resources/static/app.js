/* Viva Mama - Front multi páginas (Spring + JWT)
   Versão com:
   - Médico: ranking + filtros na lista de pacientes (medico.html)
   - Médico: nova opção no header "Meus pacientes" (Chat.html), ordenados por nº de mensagens
*/

const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];

const API = window.location.pathname.replace(/\/[^\/]*$/, ""); // mesma origem

const STORAGE = {
	token: "vm_token",
	userId: "vm_userId",
	role: "vm_role",
};

const BANNERS = [
	{ title: "Autoexame: conheça seu corpo", text: "Observe mudanças e procure avaliação médica se notar nódulos, retrações, secreções ou alterações na pele." },
	{ title: "Mamografia salva vidas", text: "A mamografia pode detectar alterações precoces. Siga a orientação do seu médico e as diretrizes da sua região." },
	{ title: "Histórico familiar importa", text: "Informe casos em parentes próximos. Isso ajuda a definir rastreio e condutas personalizadas." },
	{ title: "Não é só nódulo", text: "Vermelhidão persistente, pele em 'casca de laranja' e dor localizada também precisam de investigação." }
];

/* ---------------- Utils ---------------- */
function escapeHTML(str) {
	return String(str ?? "")
		.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
		.replaceAll('"', "&quot;").replaceAll("'", "&#039;");
}
function escapeAttr(str) { return escapeHTML(str).replaceAll("\n", " "); }

function toast(msg) {
	const el = $("#toast");
	if (!el) return;
	el.textContent = msg;
	el.hidden = false;
	setTimeout(() => (el.hidden = true), 2600);
}

function openModal(id) { const m = $(id); if (m) m.setAttribute("aria-hidden", "false"); }
function closeModal(id) { const m = $(id); if (m) m.setAttribute("aria-hidden", "true"); }

function setToken(token) { localStorage.setItem(STORAGE.token, token); }
function getToken() { return localStorage.getItem(STORAGE.token) || ""; }

function getUserMeta() {
	return {
		token: getToken(),
		userId: Number(localStorage.getItem(STORAGE.userId) || 0) || null,
		role: localStorage.getItem(STORAGE.role) || ""
	};
}

function clearAuth() {
	localStorage.removeItem(STORAGE.token);
	localStorage.removeItem(STORAGE.userId);
	localStorage.removeItem(STORAGE.role);
}

function normalizeRole(roleStr) {
	const r = (roleStr || "").toUpperCase();
	if (r === "PACIENTE" || r === "ROLE_PACIENTE") return "PACIENTE";
	if (r === "MEDICO" || r === "MÉDICO" || r === "ROLE_MEDICO") return "MEDICO";
	return r;
}

function normalizeGeneroChar(v) {
	const s = String(v ?? "").trim().toUpperCase();
	if (!s) return "N";
	const c = s[0];
	if (["F", "M", "O", "N"].includes(c)) return c;
	return "N";
}
function generoLabel(c) {
	const g = normalizeGeneroChar(c);
	if (g === "F") return "Feminino";
	if (g === "M") return "Masculino";
	if (g === "O") return "Outro";
	return "Não informado";
}

const HISTORICO_PERGUNTAS = {
	1: "Alguém da sua família já teve câncer de mama ou de ovário? Quem?",
	2: "Idade aproximada do diagnóstico",
	3: "Outros tipos de câncer na família",
	4: "Teste genético (BRCA1/BRCA2) / resultado",
	5: "Câncer de mama em homens na família?",
	6: "Mamografia/ultrassom/autoexame (último)",
	7: "Nódulos/biópsias/cirurgias/alterações",
	8: "Reposição hormonal/anticoncepcional por muito tempo",
	9: "Gestação/amamentação (tempo)",
	10: "Outros problemas de saúde importantes",
};

function parseHistoricoRespostas(textoHistorico) {
	const t = String(textoHistorico || "");
	const answers = {};
	t.split(/\r?\n/).forEach(line => {
		const m = line.trim().match(/^(\d{1,2})\)\s*(.*)$/);
		if (m) answers[Number(m[1])] = (m[2] || "").trim();
	});
	return answers;
}

function historicoTextoParaHtml(textoHistorico) {
	const t = String(textoHistorico || "").trim();
	if (!t) return "<span class='muted'>(sem texto)</span>";

	const ans = parseHistoricoRespostas(t);
	const has = Object.keys(ans).length > 0;

	// Se não conseguimos parsear pelo padrão "1) ...", cai para um bloco preformatado
	if (!has) return `<pre class="pre">${escapeHTML(t)}</pre>`;

	const item = (n) => `
    <li>
      <div class="qa__q">${escapeHTML(HISTORICO_PERGUNTAS[n] || ("Pergunta " + n))}</div>
      <div class="qa__a">${escapeHTML(ans[n] || "—")}</div>
    </li>
  `;

	return `
    <div class="hf">
      <div class="muted" style="margin-top:2px;">HISTÓRICO FAMILIAR (FAMÍLIA)</div>
      <ol class="qa" start="1">
        ${[1, 2, 3, 4, 5].map(item).join("")}
      </ol>

      <div class="muted">HISTÓRICO PESSOAL (VOCÊ)</div>
      <ol class="qa" start="6">
        ${[6, 7, 8, 9, 10].map(item).join("")}
      </ol>
    </div>
  `;
}

function goPanelByRole(role) {
	const r = normalizeRole(role);
	window.location.href = r === "PACIENTE" ? "paciente.html" : "medico.html";
}

function qs(name) {
	const url = new URL(window.location.href);
	return url.searchParams.get(name);
}

function extractServerMessage(err) {
	if (!err) return "";

	if (typeof err?.data === "string") {
		const s = err.data.trim();
		if (s) return s;
	}

	if (err?.data && typeof err.data === "object" && typeof err.data.message === "string") {
		const s = err.data.message.trim();
		if (s) return s;
	}

	const raw = String(err?.message || "");
	const m = raw.match(/^HTTP\s+\d+\s*:\s*(.*)$/i);
	const s = (m ? m[1] : raw).trim();

	if (!s) return "";
	if (["erro", "error"].includes(s.toLowerCase())) return "";
	return s;
}

function inferFriendlyError(err) {
	const status = err?.status;
	const serverMsg = extractServerMessage(err);

	if (serverMsg) return serverMsg;

	const msg = String(err?.message || err || "");

	if (status === 401 || msg.includes("HTTP 401")) return "Sessão inválida/expirada. Faça login novamente.";
	if (status === 403 || msg.includes("HTTP 403")) return "Acesso negado. Verifique o perfil/autorizações.";
	if (status === 404 || msg.includes("HTTP 404")) return "Recurso não encontrado (404).";
	if (status === 303 || msg.includes("HTTP 303")) return "Servidor retornou redirecionamento (303).";

	return msg || "Erro inesperado.";
}

function handleAuthFailure(err) {
	const status = err?.status;
	const msg = String(err?.message || "");

	if (status === 401 || msg.includes("HTTP 401")) {
		clearAuth();
		toast(inferFriendlyError(err));
		setTimeout(() => (window.location.href = "index.html"), 250);
		return true;
	}
	return false;
}

/* ---------------- Fetch com JWT ---------------- */
async function apiFetch(path, options = {}) {
	const token = getToken();

	const headers = new Headers(options.headers || {});
	headers.set("Accept", "application/json");

	const isFormData = (typeof FormData !== "undefined") && (options.body instanceof FormData);
	if (options.body && !isFormData && !headers.has("Content-Type")) {
		headers.set("Content-Type", "application/json");
	}

	const isAuthRoute = path.startsWith("/auth/");
	if (!isAuthRoute && token && !headers.has("Authorization")) {
		headers.set("Authorization", `Bearer ${token}`);
	}

	const res = await fetch(path, { ...options, headers });

	if (res.status === 401 || res.status === 403) {
		const msg = await res.text().catch(() => "");
		const err = new Error(msg || (res.status === 401 ? "Não autenticado." : "Acesso negado."));
		err.status = res.status;
		throw err;
	}

	if (res.status === 204) return null;

	const txt = await res.text();
	let data = null;
	try { data = txt ? JSON.parse(txt) : null; } catch { data = txt; }

	if (!res.ok) {
		const err = new Error((data && (data.message || data.error)) ? (data.message || data.error) : `Erro (${res.status}).`);
		err.status = res.status;
		err.data = data;
		throw err;
	}

	return data;
}

/* ---------------- Auth API ---------------- */
async function loginUser(email, senha) {
	const safeEmail = (email || "").trim().toLowerCase();
	const data = await apiFetch("/auth/login", {
		method: "POST",
		body: JSON.stringify({ email: safeEmail, senha }),
	});
	setToken(data.token);
	setUserMeta(data.userId, data.role);
	return data;
}

function setUserMeta(userId, role) {
	localStorage.setItem(STORAGE.userId, String(userId ?? ""));
	localStorage.setItem(STORAGE.role, normalizeRole(role));
}

async function registerUser(payload) {
	const p = { ...(payload || {}) };
	if (p.email) p.email = String(p.email).trim().toLowerCase();
	return apiFetch("/auth/register", {
		method: "POST",
		body: JSON.stringify(p),
	});
}

async function createPacienteProfile(payload) {
	return apiFetch("/profiles/paciente", {
		method: "POST",
		body: JSON.stringify(payload),
	});
}

async function createMedicoProfile(payload) {
	return apiFetch("/profiles/medico", {
		method: "POST",
		body: JSON.stringify(payload),
	});
}

/* ---------------- Paciente API ---------------- */
async function fetchMePaciente() {
	return apiFetch("/pacientes/me", { method: "GET" });
}
async function updateMePaciente(payload) {
	return apiFetch("/pacientes/me", { method: "PUT", body: JSON.stringify(payload) });
}

/* ---------------- Médico API ---------------- */
async function fetchPacientes() {
	return apiFetch("/pacientes", { method: "GET" });
}
async function fetchPacienteById(id) {
	return apiFetch(`/pacientes/${id}`, { method: "GET" });
}

/* ---------------- Histórico Familiar (snapshot) ---------------- */
async function fetchHistoricosByPacienteId(idPaciente) {
	return apiFetch(`/historico-familiar/paciente/${idPaciente}`, { method: "GET" });
}

async function createHistoricoSnapshot({ pacienteId, textoHistorico }) {
	return apiFetch("/historico-familiar/snapshots", {
		method: "POST",
		body: JSON.stringify({ pacienteId, textoHistorico }),
	});
}

async function downloadHistoricoAnexo(anexoId, nomeOriginal) {
	const token = getToken();
	if (!token) throw new Error("Faça login para baixar anexos.");

	const res = await fetch(API + `/historico-familiar/anexos/${anexoId}`, {
		headers: { "Authorization": `Bearer ${token}` },
		redirect: "follow"
	});

	if (!res.ok) {
		const msg = await res.text().catch(() => "");
		throw new Error(msg || `Erro ${res.status}`);
	}

	const blob = await res.blob();
	const url = URL.createObjectURL(blob);
	const a = document.createElement("a");
	a.href = url;
	a.download = nomeOriginal || `anexo-${anexoId}`;
	document.body.appendChild(a);
	a.click();
	a.remove();
	setTimeout(() => URL.revokeObjectURL(url), 1000);
}

/* ---------------- Exames / Chat ---------------- */
async function fetchExamesByPacienteId(idPaciente) {
	return apiFetch(`/exames/paciente/${idPaciente}`, { method: "GET" });
}

async function startChat(pacienteId) {
	return apiFetch("/chats/start", {
		method: "POST",
		body: JSON.stringify({ pacienteId })
	});
}

async function fetchChats() {
	return apiFetch("/chats");
}

async function fetchChatMessages(chatId) {
	return apiFetch(`/chats/${chatId}/messages`);
}

async function sendChatMessage(chatId, texto) {
	return apiFetch(`/chats/${chatId}/messages`, {
		method: "POST",
		body: JSON.stringify({ texto })
	});
}

async function uploadExame({ pacienteId, descricao, file }) {
	const fd = new FormData();
	fd.append("pacienteId", String(pacienteId));
	fd.append("descricao", String(descricao || ""));
	fd.append("arquivo", file);
	return apiFetch("/exames", { method: "POST", body: fd });
}

async function downloadExame(exameId, nomeOriginal) {
	const token = getToken();
	if (!token) throw new Error("Faça login para baixar anexos.");

	const res = await fetch(API + `/exames/${exameId}`, {
		headers: { "Authorization": `Bearer ${token}` },
		redirect: "follow"
	});

	if (!res.ok) {
		const msg = await res.text().catch(() => "");
		throw new Error(msg || `Erro ${res.status}`);
	}

	const blob = await res.blob();
	const url = URL.createObjectURL(blob);
	const a = document.createElement("a");
	a.href = url;
	a.download = nomeOriginal || `exame-${exameId}`;
	document.body.appendChild(a);
	a.click();
	a.remove();
	setTimeout(() => URL.revokeObjectURL(url), 1000);
}

/* ---------------- Layout ---------------- */
function logoSVG() {
	return `
    <svg class="brand__logo" viewBox="0 0 220 140" aria-hidden="true">
      <circle cx="110" cy="36" r="16" fill="none" stroke="var(--pink)" stroke-width="3"/>
      <path d="M60 115V78c0-10 8-18 18-18h64c10 0 18 8 18 18v37"
            fill="none" stroke="var(--pink)" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M92 82c0-6 5-10 11-10 5 0 8 3 11 7 3-4 6-7 11-7 6 0 11 4 11 10 0 13-22 26-22 26S92 95 92 82Z"
            fill="none" stroke="var(--pink)" stroke-width="3" stroke-linejoin="round"/>
      <path d="M140 98c-10-7-14-12-14-18 0-3 2-6 6-6 4 0 6 3 6 6 0 8-9 13-18 20"
            fill="none" stroke="var(--pink-soft)" stroke-width="3" stroke-linecap="round"/>
    </svg>
  `;
}

function buildLayout(page) {
	const meta = getUserMeta();
	const logged = !!meta.token;
	const role = normalizeRole(meta.role || "");
	const painelHref = logged ? (role === "PACIENTE" ? "paciente.html" : "medico.html") : "#";

	const showHistoricoMenu = logged && role === "PACIENTE";
	const showExamesMenu = logged && role === "PACIENTE";
	const showChatMenu = logged && role === "PACIENTE";

	const perfilLabel = role === "PACIENTE" ? "Paciente" : (role === "MEDICO" ? "Médico(a)" : "");

	return `
    <header class="header">
      <div class="container header__inner">
        <a class="brand" href="index.html">
          ${logoSVG()}
          <div class="brand__text">
            <div class="brand__name">VIVA MAMA</div>
            <div class="brand__tag">Apoio e acompanhamento em saúde mamária</div>
          </div>
        </a>

        <nav class="nav">
          <a class="nav__link" href="index.html">Início</a>
          <a class="nav__link" id="navCadastro" href="cadastro.html" ${logged ? "hidden" : ""}>Cadastro</a>

          <a class="nav__link" id="navHistorico" href="HistoricoFamiliar.html" ${showHistoricoMenu ? "" : "hidden"}>Histórico Familiar</a>
          <a class="nav__link" id="navExames" href="Exames.html" ${showExamesMenu ? "" : "hidden"}>Exames</a>
          <a class="nav__link" id="navChat" href="Chat.html" ${showChatMenu ? "" : "hidden"}>Chat</a>
          <a class="nav__link" id="navPainel" href="${painelHref}" ${logged ? "" : "hidden"}>Painel</a>

          <span class="badge" id="navPerfil" ${logged ? "" : "hidden"}>Área de <strong>${escapeHTML(perfilLabel)}</strong></span>

          <button class="btn btn--ghost" id="btnLogin" ${logged ? "hidden" : ""}>Entrar</button>
          <button class="btn" id="btnLogout" ${logged ? "" : "hidden"}>Sair</button>
        </nav>
      </div>
    </header>

    <main class="main">
      <div class="container">
        <div class="toast" id="toast" aria-live="polite" aria-atomic="true" hidden></div>
        <div id="pageRoot"></div>
      </div>
    </main>

    <div class="modal" id="modalLogin" aria-hidden="true">
      <div class="modal__backdrop" data-close="true"></div>
      <div class="modal__panel" role="dialog" aria-modal="true" aria-labelledby="loginTitle">
        <div class="modal__header">
          <h2 id="loginTitle">Entrar</h2>
          <button class="icon-btn" id="btnCloseLogin" aria-label="Fechar">✕</button>
        </div>

        <form id="formLogin" class="form">
          <div class="field">
            <label for="loginEmail">E-mail</label>
            <input id="loginEmail" type="email" placeholder="ex: seuemail@email.com" required />
          </div>

          <div class="field">
            <label for="loginSenha">Senha</label>
            <input id="loginSenha" type="password" placeholder="••••••••" minlength="6" required />
          </div>

          <button class="btn btn--block" type="submit">Entrar</button>

          <p class="muted">
            Não tem conta? <a href="cadastro.html">Ir para cadastro</a>
          </p>
        </form>
      </div>
    </div>
  `;
}

function bindCommon() {
	$("#btnLogin")?.addEventListener("click", () => openModal("#modalLogin"));
	$("#btnCloseLogin")?.addEventListener("click", () => closeModal("#modalLogin"));
	$("#modalLogin")?.addEventListener("click", (e) => {
		if (e.target?.dataset?.close === "true") closeModal("#modalLogin");
	});

	$("#btnLogout")?.addEventListener("click", () => {
		clearAuth();
		toast("Você saiu.");
		setTimeout(() => (window.location.href = "index.html"), 200);
	});

	$("#formLogin")?.addEventListener("submit", async (e) => {
		e.preventDefault();
		const email = ($("#loginEmail")?.value || "").trim().toLowerCase();
		const senha = $("#loginSenha")?.value || "";

		try {
			const data = await loginUser(email, senha);
			closeModal("#modalLogin");
			toast("Login efetuado!");
			setTimeout(() => goPanelByRole(data.role), 150);
		} catch (err) {
			toast(inferFriendlyError(err));
		}
	});
}

/* ---------------- Pages ---------------- */
function renderHome() {
	const root = $("#pageRoot");

	root.innerHTML = `
  <section class="hero">
      <div class="hero__top hero__top--center">
        <div>
          <h1 class="hero__title">Acolhimento, organização e cuidado.</h1>
          <p class="hero__desc">
            Um ambiente para tirar dúvidas e compartilhar conhecimento.
          </p>
        </div>
      </div>

      <div class="hr"></div>

      <div class="home-center">
        <div class="card card--flat home-card">
          <div class="card__header">
            <h2 class="card__title">Saúde das Mamas</h2>
            <p class="card__subtitle">Saiba mais sobre as suas mamas.</p>
          </div>
          <div class="card__body">
            <div class="banner">
              <div class="banner__dot"></div>
              <div>
                <h3 class="banner__title" id="bannerTitle"></h3>
                <p class="banner__text" id="bannerText"></p>
              </div>
            </div>

            <div style="margin-top:10px; display:flex; gap:10px; flex-wrap:wrap;">
              <button class="btn btn--ghost" id="btnBannerPrev">◀</button>
              <button class="btn btn--ghost" id="btnBannerNext">▶</button>
            </div>
          </div>
        </div>
      </div>
    </section>
  `;

	let idx = 0;
	const paint = () => {
		$("#bannerTitle").textContent = BANNERS[idx].title;
		$("#bannerText").textContent = BANNERS[idx].text;
	};
	paint();
	$("#btnBannerPrev").onclick = () => { idx = (idx - 1 + BANNERS.length) % BANNERS.length; paint(); };
	$("#btnBannerNext").onclick = () => { idx = (idx + 1) % BANNERS.length; paint(); };
}

/* -------- Cadastro (simples) -------- */
function renderCadastro() {
	const root = $("#pageRoot");
	root.innerHTML = `
    <div class="page-center">
      <div class="card">
        <div class="card__header">
          <h2 class="card__title">Cadastro</h2>
          <p class="card__subtitle">Selecione Paciente ou Médico(a).</p>
        </div>
        <div class="card__body">
          <form id="formCadastro" class="form">

            <div class="row">
              <div class="field">
                <label>Tipo de usuário</label>
                <select id="cadTipo" required>
                  <option value="" selected disabled>Selecione...</option>
                  <option value="PACIENTE">Paciente</option>
                  <option value="MEDICO">Médico(a)</option>
                </select>
              </div>
              <div class="field">
                <label>Telefone</label>
                <input id="cadTelefone" required placeholder="(xx) xxxxx-xxxx" />
              </div>
            </div>

            <div class="row">
              <div class="field">
                <label>E-mail</label>
                <input id="cadEmail" type="email" required placeholder="ex: voce@email.com" />
              </div>
              <div class="field">
                <label>Senha</label>
                <input id="cadSenha" type="password" minlength="6" required placeholder="mínimo 6 caracteres" />
              </div>
            </div>

            <div id="camposPaciente" hidden>
              <div class="hr"></div>
              <div class="badge">Paciente</div>

              <div class="row" style="margin-top:10px;">
                <div class="field">
                  <label>Nome</label>
                  <input id="pNome" required placeholder="Nome completo" />
                </div>
                <div class="field">
                  <label>Data de nascimento</label>
                  <input id="pNasc" type="date" />
                </div>
              </div>

              <div class="row">
                <div class="field">
                  <label>Gênero</label>
                  <select id="pGenero">
                    <option value="F">Feminino</option>
                    <option value="M">Masculino</option>
                    <option value="O">Outro</option>
                  </select>
                </div>
                <div class="field">
                  <label>Observações</label>
                  <input id="pObs" placeholder="(opcional)" />
                </div>
              </div>
            </div>

            <div id="camposMedico" hidden>
              <div class="hr"></div>
              <div class="badge">Médico</div>

              <div class="row" style="margin-top:10px;">
                <div class="field">
                  <label>Nome</label>
                  <input id="mNome" required placeholder="Nome completo" />
                </div>
                <div class="field">
                  <label>CRM</label>
                  <input id="mCrm" required placeholder="CRM-UF 12345" />
                </div>
              </div>

              <div class="field">
                <label>Especialidade</label>
                <input id="mEsp" required placeholder="Mastologia, Ginecologia..." />
              </div>
            </div>

            <button class="btn btn--block" type="submit">Criar conta</button>
            <p class="muted">Já tem conta? <a href="#" id="cadIrLogin">Entrar</a></p>
          </form>
        </div>
      </div>
    </div>
  `;

	const cadTipo = $("#cadTipo");
	const camposPaciente = $("#camposPaciente");
	const camposMedico = $("#camposMedico");

	function setGroupActive(groupEl, active) {
		groupEl.hidden = !active;
		$$("input, select, textarea", groupEl).forEach(el => {
			el.disabled = !active;
		});
	}

	function applyTipo(t) {
		const isPaciente = t === "PACIENTE";
		const isMedico = t === "MEDICO";

		setGroupActive(camposPaciente, isPaciente);
		setGroupActive(camposMedico, isMedico);

		$("#pNome").required = isPaciente;

		$("#mNome").required = isMedico;
		$("#mCrm").required = isMedico;
		$("#mEsp").required = isMedico;
	}

	cadTipo.addEventListener("change", () => applyTipo(cadTipo.value));
	applyTipo(cadTipo.value);

	$("#cadIrLogin").addEventListener("click", (e) => {
		e.preventDefault();
		openModal("#modalLogin");
	});

	$("#formCadastro").addEventListener("submit", async (e) => {
		e.preventDefault();

		const role = normalizeRole($("#cadTipo").value);
		const telefone = $("#cadTelefone").value.trim();
		const email = $("#cadEmail").value.trim().toLowerCase();
		const senha = $("#cadSenha").value;

		if (!role) return toast("Selecione o tipo de usuário.");
		if (!telefone) return toast("Telefone é obrigatório.");

		try {
			const regPayload = { email, senha, telefone, role };

			if (role === "PACIENTE") {
				regPayload.nome = $("#pNome").value.trim();
				regPayload.dataNascimento = $("#pNasc").value || null;
				regPayload.observacoes = $("#pObs").value.trim();
				regPayload.genero = normalizeGeneroChar($("#pGenero").value);
			}

			if (role === "MEDICO") {
				regPayload.nome = $("#mNome").value.trim();
				regPayload.crm = $("#mCrm").value.trim();
				regPayload.especialidade = $("#mEsp").value.trim();
			}

			await registerUser(regPayload);

			const auth = await loginUser(email, senha);
			const r = normalizeRole(auth.role);

			if (r === "PACIENTE") {
				const nome = $("#pNome").value.trim();
				if (!nome) return toast("Nome é obrigatório.");

				await createPacienteProfile({
					nome,
					dataNascimento: $("#pNasc").value || null,
					historicoFamiliar: "",
					observacoes: $("#pObs").value.trim(),
					genero: normalizeGeneroChar($("#pGenero").value),
				});
			}

			if (r === "MEDICO") {
				const nome = $("#mNome").value.trim();
				const crm = $("#mCrm").value.trim();
				const especialidade = $("#mEsp").value.trim();

				if (!nome) return toast("Nome do médico é obrigatório.");
				if (!crm) return toast("CRM é obrigatório.");
				if (!especialidade) return toast("Especialidade é obrigatória.");

				await createMedicoProfile({ nome, crm, especialidade });
			}

			toast("Conta criada! Indo para o painel...");
			setTimeout(() => goPanelByRole(r), 250);

		} catch (err) {
			toast(inferFriendlyError(err));
		}
	});
}

/* -------- Paciente (dados pessoais sempre) -------- */
function renderPaciente() {
	const meta = getUserMeta();
	const role = normalizeRole(meta.role || "");
	if (!meta.token) {
		toast("Faça login para acessar.");
		return setTimeout(() => window.location.href = "index.html", 250);
	}
	if (role !== "PACIENTE") {
		toast("Acesso restrito.");
		return setTimeout(() => window.location.href = "index.html", 250);
	}

	const root = $("#pageRoot");
	root.innerHTML = `
    <div class="card card--flat">
      <div class="card__header">
        <h2 class="card__title">Área do Paciente</h2>
        <p class="card__subtitle">Visualize e altere seus dados pessoais. O histórico fica na página “Histórico Familiar”.</p>
      </div>
      <div class="card__body">
        <div class="muted" id="pLoading">Carregando...</div>
        <div id="pContent" hidden></div>
      </div>
    </div>
  `;

	async function loadMe() {
		try {
			const me = await fetchMePaciente();
			renderPerfilForm(me);
		} catch (err) {
			$("#pLoading").textContent = inferFriendlyError(err);
		}
	}

	function renderPerfilForm(me) {
		$("#pLoading").hidden = true;
		const box = $("#pContent");
		box.hidden = false;

		box.innerHTML = `
      <form id="formPaciente" class="form">
        <div class="row">
          <div class="field" style="flex:1;">
            <label>Nome</label>
            <input id="pNome" value="${escapeAttr(me.nome || "")}" required />
          </div>
          <div class="field" style="flex:1;">
            <label>Data de nascimento</label>
            <input id="pNasc" type="date" value="${escapeAttr(me.dataNascimento || "")}" />
          </div>
        </div>

        <div class="row">
          <div class="field" style="flex:1;">
            <label>Gênero</label>
            <select id="pGenero">
              <option value="F" ${me.genero === "F" ? "selected" : ""}>Feminino</option>
              <option value="M" ${me.genero === "M" ? "selected" : ""}>Masculino</option>
              <option value="O" ${me.genero === "O" ? "selected" : ""}>Outro</option>
            </select>
          </div>

          <div class="field" style="flex:1;">
            <label>Observações</label>
            <input id="pObs" value="${escapeAttr(me.observacoes || "")}" />
          </div>
        </div>

        <div class="row">
          <button class="btn" type="submit">Salvar</button>
          <a class="btn btn--ghost" href="HistoricoFamiliar.html">Abrir Histórico Familiar</a>
        </div>

        <div class="muted">O histórico familiar (texto + exames) é versionado na página Histórico Familiar.</div>
      </form>
    `;

		$("#formPaciente").addEventListener("submit", async (e) => {
			e.preventDefault();
			try {
				const data = {
					nome: $("#pNome").value.trim(),
					dataNascimento: $("#pNasc").value || null,
					genero: normalizeGeneroChar($("#pGenero").value),
					observacoes: $("#pObs").value.trim(),
				};

				await updateMePaciente(data);
				toast("Dados salvos!");
			} catch (err) {
				toast(inferFriendlyError(err));
			}
		});
	}

	loadMe();
}

/* -------- Chat/Meus pacientes (mesma página Chat.html) -------- */
function renderChatHub() {

	const meta = getUserMeta();
	const role = normalizeRole(meta.role || "");
	if (!meta.token) return (toast("Faça login."), setTimeout(() => location.href = "index.html", 250));

	if (role === "MEDICO") {
		toast("Acesse um paciente e use o chat por lá.");
		return setTimeout(() => (location.href = "medico.html"), 250);
	}
	
	if (!meta.token) return (toast("Faça login."), setTimeout(() => location.href = "index.html", 250));

	const isMedico = role === "MEDICO";

	const root = $("#pageRoot");
	root.innerHTML = `
    <div class="page-center">
      <div class="card">
        <div class="card__header">
          <h2 class="card__title">${isMedico ? "Meus pacientes" : "Chat"}</h2>
          <p class="card__subtitle">${isMedico ? "Pacientes com conversa iniciada, ordenados por mensagens." : "Conversas."}</p>
        </div>

        <div class="card__body">
          <div class="chat-wrap">
            <div class="chat-list" id="chatList">
              <div class="muted">Carregando chats...</div>
            </div>

            <div class="chat-thread">
              <div class="chat-messages" id="chatMsgs">
                <div class="muted">Selecione uma conversa.</div>
              </div>

              <form class="chat-input" id="chatForm">
                <input id="chatText" placeholder="Digite sua mensagem..." required />
                <button class="btn" type="submit">Enviar</button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;

	let currentChatId = null;
	let pollTimer = null;

	function stopPoll() {
		if (pollTimer) clearInterval(pollTimer);
		pollTimer = null;
	}

	async function loadMessages(chatId) {
		const box = $("#chatMsgs");
		const msgs = await fetchChatMessages(chatId);

		const meSender = role === "PACIENTE" ? "PACIENTE" : "MEDICO";

		box.innerHTML = (msgs || []).map(m => {
			const mine = m.sender === meSender;
			return `
        <div class="bubble ${mine ? "bubble--me" : "bubble--other"}">
          <div class="bubble__text">${escapeHTML(m.texto || "")}</div>
          <div class="bubble__meta">${escapeHTML(m.enviadoEm ? new Date(m.enviadoEm).toLocaleString() : "")}</div>
        </div>
      `;
		}).join("") || `<div class="muted">Nenhuma mensagem.</div>`;

		box.scrollTop = box.scrollHeight;
	}

	async function selectChat(chatId) {
		currentChatId = chatId;
		await loadMessages(chatId);

		stopPoll();
		pollTimer = setInterval(() => {
			if (currentChatId) loadMessages(currentChatId).catch(() => { });
		}, 3000);
	}

	async function loadChats() {
		const listBox = $("#chatList");
		try {
			let chats = await fetchChats();

			if (!Array.isArray(chats) || chats.length === 0) {
				listBox.innerHTML = `<div class="muted">Nenhuma conversa ainda.</div>`;
				return;
			}

			// ✅ Médico: ordena por quantidade de mensagens (fallback se API não retorna contagem)
			if (isMedico && chats.length) {
				const enriched = await Promise.all(chats.map(async (c) => {
					let count = c.messagesCount ?? c.mensagensCount ?? c.qtdMensagens;
					let lastAt = c.lastMessageAt ?? c.ultimaMensagemEm;

					if (count == null || lastAt == null) {
						try {
							const msgs = await fetchChatMessages(c.chatId);
							const arr = Array.isArray(msgs) ? msgs : [];
							if (count == null) count = arr.length;

							if (lastAt == null && arr.length) {
								const maxTs = arr.reduce((max, m) => {
									const t = m?.enviadoEm ? new Date(m.enviadoEm).getTime() : 0;
									return Math.max(max, t);
								}, 0);
								lastAt = maxTs ? new Date(maxTs).toISOString() : null;
							}
						} catch {
							count = count ?? 0;
							lastAt = lastAt ?? null;
						}
					}

					return { ...c, __count: Number(count) || 0, __lastAt: lastAt };
				}));

				enriched.sort((a, b) => {
					const dc = (b.__count - a.__count);
					if (dc !== 0) return dc;
					const ta = (a.__lastAt ? new Date(a.__lastAt).getTime() : 0);
					const tb = (b.__lastAt ? new Date(b.__lastAt).getTime() : 0);
					return tb - ta;
				});

				chats = enriched;
			}

			listBox.innerHTML = chats.map(c => {
				const title =
					role === "PACIENTE"
						? (c.medicoLabel || "Médico(a)")
						: (c.pacienteNome || "Paciente");

				const sub = isMedico
					? `${(c.__count ?? c.messagesCount ?? 0)} mensagens`
					: (c.criadoEm ? new Date(c.criadoEm).toLocaleString() : "");

				return `
          <button class="chat-item" type="button" data-chat-id="${c.chatId}">
            <div class="chat-item__title">${escapeHTML(title)}</div>
            <div class="chat-item__meta">${escapeHTML(sub)}</div>
          </button>
        `;
			}).join("");

			$$(".chat-item", listBox).forEach(btn => {
				btn.addEventListener("click", async () => {
					$$(".chat-item", listBox).forEach(x => x.classList.remove("chat-item--active"));
					btn.classList.add("chat-item--active");
					await selectChat(Number(btn.dataset.chatId));
				});
			});

			const qChatId = Number(new URLSearchParams(location.search).get("chatId") || 0);
			const firstId = qChatId || Number(chats[0].chatId);

			const firstBtn = listBox.querySelector(`[data-chat-id="${firstId}"]`);
			if (firstBtn) firstBtn.classList.add("chat-item--active");
			await selectChat(firstId);

		} catch (err) {
			listBox.innerHTML = `<div class="muted">${escapeHTML(inferFriendlyError(err))}</div>`;
		}
	}

	$("#chatForm").addEventListener("submit", async (e) => {
		e.preventDefault();
		if (!currentChatId) return toast("Selecione uma conversa.");

		const input = $("#chatText");
		const txt = input.value.trim();
		if (!txt) return;

		try {
			await sendChatMessage(currentChatId, txt);
			input.value = "";
			await loadMessages(currentChatId);
		} catch (err) {
			toast(inferFriendlyError(err));
		}
	});

	loadChats();
	window.addEventListener("beforeunload", stopPoll);
}

/* -------- Médico (lista pacientes com ranking + filtros) -------- */
function renderMedico() {
	const meta = getUserMeta();
	const role = normalizeRole(meta.role || "");
	if (!meta.token) {
		toast("Faça login para acessar.");
		return setTimeout(() => window.location.href = "index.html", 250);
	}
	if (role !== "MEDICO") {
		toast("Acesso restrito.");
		return setTimeout(() => window.location.href = "index.html", 250);
	}

	const root = $("#pageRoot");
	root.innerHTML = `
  <div class="page-center">
    <div class="card">
      <div class="card__header">
        <h2 class="card__title">Pacientes cadastrados</h2>
        <p class="card__subtitle">Busque e abra um paciente para ver histórico familiar, exames e chat.</p>
      </div>
      <div class="card__body">

        <div class="row">
          <div class="field" style="flex:1;">
            <label>Buscar</label>
            <input id="mBusca" placeholder="nome..." />
          </div>
          <div class="field" style="width:220px;">
            <label>&nbsp;</label>
            <button class="btn btn--ghost" id="btnReloadPacientes" type="button">Recarregar</button>
          </div>
        </div>

        <!-- ✅ Ranking + filtros (sessão da imagem) -->
        <div class="row" style="margin-top:10px; flex-wrap:wrap;">
          <div class="field" style="width:240px;">
            <label>Ordenar (ranking)</label>
            <select id="mOrdenar">
              <option value="nome_asc">Nome (A–Z)</option>
              <option value="nome_desc">Nome (Z–A)</option>
              <option value="idade_desc">Idade (maior primeiro)</option>
              <option value="idade_asc">Idade (menor primeiro)</option>
            </select>
          </div>

          <div class="field" style="width:220px;">
            <label>Filtro: Gênero</label>
            <select id="mFiltroGenero">
              <option value="TODOS">Todos</option>
              <option value="F">Feminino</option>
              <option value="M">Masculino</option>
              <option value="O">Outro</option>     
            </select>
          </div>

          <div class="field" style="width:220px;">
            <label>Filtro: Faixa etária</label>
            <select id="mFiltroIdade">
              <option value="TODOS">Todas</option>
              <option value="0-29">Até 29</option>
              <option value="30-39">30–39</option>
              <option value="40-49">40–49</option>
              <option value="50+">50+</option>
            </select>
          </div>
        </div>

        <div class="hr"></div>
        <div id="mLista" class="list">
          <div class="muted">Carregando...</div>
        </div>
      </div>
    </div>
  </div>
  `;

	const state = {
		pacientes: [],
		sort: "nome_asc",
		filtroGenero: "TODOS",
		filtroIdade: "TODOS",
	};

	function calcAge(dateStr) {
		if (!dateStr) return null;
		const d = new Date(dateStr);
		if (Number.isNaN(d.getTime())) return null;
		const now = new Date();
		let age = now.getFullYear() - d.getFullYear();
		const m = now.getMonth() - d.getMonth();
		if (m < 0 || (m === 0 && now.getDate() < d.getDate())) age--;
		return age >= 0 && age < 130 ? age : null;
	}

	function inAgeFilter(age, filtro) {
		if (filtro === "TODOS") return true;
		if (age == null) return false;
		if (filtro === "0-29") return age <= 29;
		if (filtro === "30-39") return age >= 30 && age <= 39;
		if (filtro === "40-49") return age >= 40 && age <= 49;
		if (filtro === "50+") return age >= 50;
		return true;
	}

	async function loadPacientes() {
		const list = $("#mLista");
		list.innerHTML = `<div class="muted">Carregando...</div>`;
		try {
			const pacientes = await fetchPacientes();
			state.pacientes = Array.isArray(pacientes) ? pacientes : [];
			renderLista();
		} catch (err) {
			list.innerHTML = `<div class="muted">${escapeHTML(inferFriendlyError(err))}</div>`;
		}
	}

	function renderLista() {
		const list = $("#mLista");
		const q = ($("#mBusca").value || "").trim().toLowerCase();
		const collator = new Intl.Collator("pt-BR", { sensitivity: "base" });

		let arr = (state.pacientes || []).map(p => {
			const idade = calcAge(p.dataNascimento);
			const gen = normalizeGeneroChar(p.genero);
			return { ...p, __idade: idade, __gen: gen };
		});

		// busca por nome
		arr = arr.filter(p => {
			const nome = String(p.nome || "").toLowerCase();
			return !q || nome.includes(q);
		});

		// filtro gênero
		if (state.filtroGenero !== "TODOS") {
			arr = arr.filter(p => p.__gen === state.filtroGenero);
		}

		// filtro idade
		arr = arr.filter(p => inAgeFilter(p.__idade, state.filtroIdade));

		// ranking/ordenação
		arr.sort((a, b) => {
			if (state.sort === "nome_asc") return collator.compare(a.nome || "", b.nome || "");
			if (state.sort === "nome_desc") return collator.compare(b.nome || "", a.nome || "");

			// idade: null vai pro fim
			const ai = (a.__idade == null) ? -1 : a.__idade;
			const bi = (b.__idade == null) ? -1 : b.__idade;

			if (state.sort === "idade_desc") return (bi - ai);
			if (state.sort === "idade_asc") return (ai - bi);

			return 0;
		});

		list.innerHTML = arr.length
			? arr.map(p => {
				const idadeTxt = p.__idade != null ? ` • ${p.__idade} anos` : "";
				const genTxt = generoLabel(p.__gen);
				return `
          <div class="item">
            <div class="item__top">
              <div>
                <p class="item__title">${escapeHTML(p.nome || "Paciente")}</p>
                <div class="item__meta">${escapeHTML(genTxt + idadeTxt)}</div>
              </div>
              <div style="display:flex; gap:8px; flex-wrap:wrap;">
                <button class="btn btn--ghost" data-open="${p.idPaciente}">Abrir</button>
              </div>
            </div>
          </div>
        `;
			}).join("")
			: `<div class="muted">Nenhum paciente encontrado.</div>`;

		$$("[data-open]", list).forEach(btn => {
			btn.addEventListener("click", () => {
				const id = Number(btn.dataset.open);
				if (!id) return;
				window.location.href = `medico_paciente.html?pacienteId=${id}`;
			});
		});
	}

	$("#mBusca").addEventListener("input", renderLista);
	$("#btnReloadPacientes").addEventListener("click", loadPacientes);

	$("#mOrdenar").addEventListener("change", (e) => {
		state.sort = e.target.value;
		renderLista();
	});
	$("#mFiltroGenero").addEventListener("change", (e) => {
		state.filtroGenero = e.target.value;
		renderLista();
	});
	$("#mFiltroIdade").addEventListener("change", (e) => {
		state.filtroIdade = e.target.value;
		renderLista();
	});

	loadPacientes();
}

/* -------- Médico Paciente (chat + drawer exames/histórico) -------- */
function renderMedicoPaciente() {
  const meta = getUserMeta();
  const role = normalizeRole(meta.role || "");
  if (!meta.token) return (toast("Faça login."), setTimeout(() => location.href = "index.html", 250));
  if (role !== "MEDICO") return (toast("Acesso restrito."), setTimeout(() => location.href = "index.html", 250));

  const pacienteId = Number(qs("pacienteId"));
  if (!pacienteId) return (toast("Paciente inválido."), setTimeout(() => location.href = "medico.html", 250));

  const root = $("#pageRoot");
  root.innerHTML = `
    <div class="mp-shell">
      <!-- ESQUERDA: detalhes do paciente -->
      <aside class="mp-left">
        <div class="card card--flat">
          <div class="card__header">
            <h2 class="card__title">Paciente</h2>
            <p class="card__subtitle" id="mpSub">Carregando...</p>
          </div>
          <div class="card__body" id="mpTop">
            <div class="muted">Carregando...</div>
          </div>
        </div>
      </aside>

      <!-- CENTRO: chat sempre visível -->
      <section class="mp-main">
        <div class="card mp-mainCard">
          <div class="card__header">
            <h2 class="card__title">Chat</h2>
            <p class="card__subtitle">Você é anônimo(a) para o paciente.</p>
          </div>

          <div class="card__body mp-mainBody">
            <div class="chat-thread">
              <div class="chat-messages" id="mpChatMsgs">
                <div class="muted">Carregando...</div>
              </div>

              <form class="chat-input" id="mpChatForm">
                <input id="mpChatText" placeholder="Digite sua mensagem..." required />
                <button class="btn" type="submit">Enviar</button>
              </form>
            </div>
          </div>
        </div>
      </section>

      <!-- DIREITA: painel de informações (toggle) -->
      <aside class="mp-right" id="mpRight">
        <div class="card card--flat mp-rightCard">
          <div class="card__header">
            <div class="mp-rightHead">
              <div>
                <h3 class="card__title">Ações</h3>
                <p class="card__subtitle">Informações do paciente</p>
              </div>
              <button class="btn btn--ghost mp-drawerClose" id="mpDrawerClose" type="button" aria-label="Fechar">✕</button>
            </div>
          </div>

          <div class="card__body">
            <div class="mp-actions">
              <a class="btn btn--ghost" href="medico.html">← Voltar</a>
              <button class="btn btn--ghost" type="button" id="mpToggleMyPatients">Meus pacientes</button>
              <button class="btn btn--ghost" type="button" id="mpToggleExames">Exames</button>
              <button class="btn btn--ghost" type="button" id="mpToggleHistorico">Histórico familiar</button>
            </div>

            <div id="mpSidePanel" class="mp-sidePanel vm-hidden">
              <div class="mp-sidePanel__header">
                <div class="mp-sideTitle" id="mpSideTitle">Exames</div>
              </div>
              <div id="mpSideContent" class="list">
              </div>
            </div>
          </div>
        </div>

        <button class="btn mp-drawerOpen" id="mpDrawerOpen" type="button">☰</button>
      </aside>
    </div>
  `;

  /* ------------------ TOP (detalhes) ------------------ */
  async function renderTop() {
    try {
      const p = await fetchPacienteById(pacienteId);
      $("#mpSub").textContent = p.nome || "Paciente";

      $("#mpTop").innerHTML = `
        <div class="item">
          <div class="muted">Nome</div><div>${escapeHTML(p.nome || "—")}</div>
          <div class="hr"></div>
          <div class="muted">Nascimento</div><div>${escapeHTML(p.dataNascimento || "—")}</div>
          <div class="hr"></div>
          <div class="muted">Observações</div><div>${escapeHTML(p.observacoes || "—")}</div>
        </div>
      `;
    } catch (err) {
      $("#mpSub").textContent = "Erro";
      $("#mpTop").innerHTML = `<div class="muted">${escapeHTML(inferFriendlyError(err))}</div>`;
    }
  }

  /* ------------------ CHAT (não cria conversa ao abrir) ------------------ */
  let chatId = null;
  let pollTimer = null;

  function stopPoll() {
    if (pollTimer) clearInterval(pollTimer);
    pollTimer = null;
  }

  function setEmptyChatHint() {
    $("#mpChatMsgs").innerHTML = `
      <div class="muted">
        Nenhuma conversa iniciada ainda.<br/>
        Envie a primeira mensagem para iniciar o atendimento.
      </div>
    `;
  }

  function parseChatId(value) {
    if (typeof value === "number") return value;
    const n = Number(value?.chatId || value?.id || value);
    return Number.isFinite(n) ? n : null;
  }

  function getPacienteIdFromChat(c) {
    const pid =
      c?.pacienteId ??
      c?.idPaciente ??
      c?.paciente?.idPaciente ??
      c?.paciente?.id ??
      c?.pacienteID;
    const n = Number(pid);
    return Number.isFinite(n) && n > 0 ? n : null;
  }

  async function findExistingChatIdForPaciente() {
    try {
      const chats = await fetchChats();
      if (!Array.isArray(chats)) return null;

      const found = chats.find(c => getPacienteIdFromChat(c) === pacienteId);
      return found ? Number(found.chatId) : null;
    } catch {
      return null;
    }
  }

  async function loadMsgs() {
    const box = $("#mpChatMsgs");
    const msgs = await fetchChatMessages(chatId);

    box.innerHTML = (msgs || []).map(m => {
      const mine = m.sender === "MEDICO";
      return `
        <div class="bubble ${mine ? "bubble--me" : "bubble--other"}">
          <div class="bubble__text">${escapeHTML(m.texto || "")}</div>
          <div class="bubble__meta">${escapeHTML(m.enviadoEm ? new Date(m.enviadoEm).toLocaleString() : "")}</div>
        </div>
      `;
    }).join("") || `<div class="muted">Nenhuma mensagem.</div>`;

    box.scrollTop = box.scrollHeight;
  }

  function startPolling() {
    stopPoll();
    pollTimer = setInterval(() => {
      if (chatId) loadMsgs().catch(() => {});
    }, 3000);
  }

  async function initChat() {
    // tenta achar chat existente (sem criar)
    chatId = await findExistingChatIdForPaciente();

    if (chatId) {
      try {
        await loadMsgs();
        startPolling();
      } catch (err) {
        $("#mpChatMsgs").innerHTML = `<div class="muted">${escapeHTML(inferFriendlyError(err))}</div>`;
      }
    } else {
      setEmptyChatHint();
    }

    $("#mpChatForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      const input = $("#mpChatText");
      const txt = input.value.trim();
      if (!txt) return;

      try {
        // ✅ só cria conversa no primeiro envio
        if (!chatId) {
          const started = await startChat(pacienteId);
          chatId = parseChatId(started);
          if (!chatId) throw new Error("Não consegui iniciar o chat.");
          startPolling();
        }

        await sendChatMessage(chatId, txt);
        input.value = "";
        await loadMsgs();
      } catch (err) {
        toast(inferFriendlyError(err));
      }
    });

    window.addEventListener("beforeunload", stopPoll);
  }

  /* ------------------ PAINEL DIREITO (toggle) ------------------ */
  let panelOpen = false;
  let panelType = null; // "mypatients" | "exames" | "historico" | null

  function isMobile() {
    return window.matchMedia("(max-width: 980px)").matches;
  }

  function openDrawer() {
    $("#mpRight").classList.add("mp-right--open");
  }

  function closeDrawer() {
    $("#mpRight").classList.remove("mp-right--open");
  }

  function updateActiveButtons() {
    $("#mpToggleMyPatients").classList.toggle("is-active", panelOpen && panelType === "mypatients");
    $("#mpToggleExames").classList.toggle("is-active", panelOpen && panelType === "exames");
    $("#mpToggleHistorico").classList.toggle("is-active", panelOpen && panelType === "historico");
  }

  function showPanel(title) {
    $("#mpSideTitle").textContent = title;
    $("#mpSidePanel").classList.remove("vm-hidden");
  }

  function hidePanel() {
    $("#mpSidePanel").classList.add("vm-hidden");
    $("#mpSideContent").innerHTML = "";
  }

  async function renderExamesInto(container) {
    container.innerHTML = `<div class="muted">Carregando exames...</div>`;
    try {
      const exames = await fetchExamesByPacienteId(pacienteId);
      if (!Array.isArray(exames) || exames.length === 0) {
        container.innerHTML = `<div class="muted">Nenhum exame enviado.</div>`;
        return;
      }

      container.innerHTML = exames.map(e => {
        const enviado = e.enviadoEm ? new Date(e.enviadoEm).toLocaleString() : "";
        const nome = e.nomeOriginal || "arquivo";
        const desc = String(e.descricao || "").trim();

        return `
          <div class="item">
            <div class="item__top">
              <div>
                <p class="item__title">Exame</p>
                <div class="item__meta">${escapeHTML(enviado)}</div>
              </div>
              <span class="badge">exame</span>
            </div>

            ${desc ? `
              <div class="muted" style="margin-top:6px;">Descrição</div>
              <div class="mp-examDesc">${escapeHTML(desc)}</div>
            ` : ""}

            <div class="hr"></div>
            <button class="btn btn--ghost" type="button"
              data-dl-exame="${e.id}" data-name="${escapeAttr(nome)}">
              📎 ${escapeHTML(nome)}
            </button>
          </div>
        `;
      }).join("");

      $$('[data-dl-exame]', container).forEach(btn => {
        btn.addEventListener("click", async () => {
          try {
            await downloadExame(btn.dataset.dlExame, btn.dataset.name || "arquivo");
          } catch (err) {
            toast(inferFriendlyError(err));
          }
        });
      });
    } catch (err) {
      container.innerHTML = `<div class="muted">${escapeHTML(inferFriendlyError(err))}</div>`;
    }
  }

  async function renderHistoricoInto(container) {
    container.innerHTML = `<div class="muted">Carregando histórico...</div>`;
    try {
      const historicos = await fetchHistoricosByPacienteId(pacienteId);
      if (!Array.isArray(historicos) || historicos.length === 0) {
        container.innerHTML = `<div class="muted">Nenhum histórico cadastrado.</div>`;
        return;
      }

      container.innerHTML = historicos.map(h => `
        <div class="item">
          <div class="item__top">
            <div>
              <p class="item__title">Histórico</p>
              <div class="item__meta">${escapeHTML(h.criadoEm ? new Date(h.criadoEm).toLocaleString() : "")}</div>
            </div>
            <span class="badge">histórico</span>
          </div>
          <div class="hr"></div>
          <div>${historicoTextoParaHtml(h.textoHistorico)}</div>
        </div>
      `).join("");
    } catch (err) {
      container.innerHTML = `<div class="muted">${escapeHTML(inferFriendlyError(err))}</div>`;
    }
  }

  // ✅ Meus pacientes: somente chats em que o MÉDICO já enviou mensagem
  async function renderMyPatientsInto(container) {
    container.innerHTML = `<div class="muted">Carregando meus pacientes...</div>`;

    try {
      const chats = await fetchChats();
      if (!Array.isArray(chats) || chats.length === 0) {
        container.innerHTML = `<div class="muted">Você ainda não tem pacientes com conversa.</div>`;
        return;
      }

      const enriched = await Promise.all(chats.map(async (c) => {
        const pid = getPacienteIdFromChat(c);
        const nome = c.pacienteNome || c.paciente?.nome || "Paciente";
        if (!pid) return null;

        let msgs = [];
        try {
          const m = await fetchChatMessages(c.chatId);
          msgs = Array.isArray(m) ? m : [];
        } catch { msgs = []; }

        const medicoMandou = msgs.some(m => m?.sender === "MEDICO");
        const count = msgs.length;

        const lastTs = msgs.reduce((max, m) => {
          const t = m?.enviadoEm ? new Date(m.enviadoEm).getTime() : 0;
          return Math.max(max, t);
        }, 0);

        return {
          pacienteId: pid,
          pacienteNome: nome,
          chatId: c.chatId,
          medicoMandou,
          count,
          lastTs
        };
      }));

      const mine = enriched
        .filter(Boolean)
        .filter(x => x.medicoMandou); // ✅ regra principal

      if (mine.length === 0) {
        container.innerHTML = `<div class="muted">Nenhum paciente atribuído ainda (envie uma mensagem para iniciar).</div>`;
        return;
      }

      mine.sort((a, b) => {
        const dc = b.count - a.count;
        if (dc !== 0) return dc;
        return (b.lastTs || 0) - (a.lastTs || 0);
      });

      container.innerHTML = mine.map(x => {
        const last = x.lastTs ? new Date(x.lastTs).toLocaleString() : "";
        return `
          <button class="chat-item" type="button" data-open-pid="${x.pacienteId}">
            <div class="chat-item__title">${escapeHTML(x.pacienteNome)}</div>
            <div class="chat-item__meta">${escapeHTML(`${x.count} mensagens${last ? " • " + last : ""}`)}</div>
          </button>
        `;
      }).join("");

      $$("[data-open-pid]", container).forEach(btn => {
        btn.addEventListener("click", () => {
          const pid = Number(btn.dataset.openPid);
          if (!pid) return;
          // ✅ mesmo comportamento do "Abrir"
          window.location.href = `medico_paciente.html?pacienteId=${pid}`;
        });
      });

    } catch (err) {
      container.innerHTML = `<div class="muted">${escapeHTML(inferFriendlyError(err))}</div>`;
    }
  }

  async function openPanel(type) {
    panelOpen = true;
    panelType = type;
    updateActiveButtons();
    if (isMobile()) openDrawer();

    const content = $("#mpSideContent");

    if (type === "mypatients") {
      showPanel("Meus pacientes");
      await renderMyPatientsInto(content);
      return;
    }
    if (type === "exames") {
      showPanel("Exames");
      await renderExamesInto(content);
      return;
    }
    showPanel("Histórico familiar");
    await renderHistoricoInto(content);
  }

  function closePanel() {
    panelOpen = false;
    panelType = null;
    updateActiveButtons();
    hidePanel();
  }

  async function togglePanel(type) {
    if (panelOpen && panelType === type) {
      closePanel();
      return;
    }
    await openPanel(type);
  }

  $("#mpToggleMyPatients").addEventListener("click", () => togglePanel("mypatients"));
  $("#mpToggleExames").addEventListener("click", () => togglePanel("exames"));
  $("#mpToggleHistorico").addEventListener("click", () => togglePanel("historico"));

  // Drawer mobile
  $("#mpDrawerOpen").addEventListener("click", openDrawer);
  $("#mpDrawerClose").addEventListener("click", closeDrawer);

  window.addEventListener("resize", () => {
    if (!isMobile()) closeDrawer();
  });

  // Inicializa
  renderTop();
  initChat();
  closePanel();
}


/* -------- Histórico Familiar -------- */
function renderHistorico() {
	const meta = getUserMeta();
	const role = normalizeRole(meta.role || "");
	if (!meta.token) {
		toast("Faça login para acessar.");
		return setTimeout(() => window.location.href = "index.html", 250);
	}

	const isPaciente = role === "PACIENTE";
	const isMedico = role === "MEDICO";

	const root = $("#pageRoot");
	root.innerHTML = `
    <div class="card card--flat">
      <div class="card__header">
        <h2 class="card__title">Histórico Familiar</h2>
        <p class="card__subtitle">
          ${isPaciente
			? "Cada vez que você salva, uma nova versão do histórico é criada. Exames ficam na página “Exames”."
			: "Apenas visualização."
		}
        </p>
      </div>

      <div class="card__body">
        <form id="formHistorico" class="form">
          <input id="hfPacienteId" type="hidden" />

          <div class="row">
            <div class="field" style="flex:1;">
              <div class="muted" id="hfHint"></div>
            </div>
          </div>

          ${isPaciente ? `
            <div class="form__section">
              <h3 class="section__title">Histórico (formulário)</h3>

              <div class="field">
                <label>1) Alguém da sua família já teve câncer de mama ou de ovário? Quem?</label>
                <textarea id="hfQ1" required></textarea>
              </div>

              <div class="field">
                <label>2) Idade aproximada do diagnóstico</label>
                <input id="hfQ2" required />
              </div>

              <div class="field">
                <label>3) Outros tipos de câncer na família</label>
                <input id="hfQ3" required />
              </div>

              <div class="field">
                <label>4) Teste genético (BRCA1/BRCA2) / resultado</label>
                <input id="hfQ4" required />
              </div>

              <div class="field">
                <label>5) Câncer de mama em homens na família?</label>
                <input id="hfQ5" required />
              </div>

              <div class="field">
                <label>6) Mamografia/ultrassom/autoexame (último)</label>
                <input id="hfQ6" required />
              </div>

              <div class="field">
                <label>7) Nódulos/biópsias/cirurgias/alterações</label>
                <input id="hfQ7" required />
              </div>

              <div class="field">
                <label>8) Reposição hormonal/anticoncepcional por muito tempo</label>
                <input id="hfQ8" required />
              </div>

              <div class="field">
                <label>9) Gestação/amamentação (tempo)</label>
                <input id="hfQ9" required />
              </div>

              <div class="field">
                <label>10) Outros problemas de saúde importantes</label>
                <input id="hfQ10" required />
              </div>

              <div class="row" style="align-items:flex-start; gap:10px; flex-wrap:wrap;">
                <button class="btn" type="submit">Salvar nova versão</button>
                <button class="btn btn--ghost" type="button" id="btnHfPrev">Exibir histórico familiar anterior</button>
              </div>

              <div id="hfPrevBox" class="item" hidden></div>
            </div>
          ` : ``}

          <div class="hr"></div>

          <div class="form__section">
            <h3 class="section__title">Versões salvas</h3>
            <div id="hfList" class="list">
              <div class="muted">Carregando...</div>
            </div>
          </div>
        </form>
      </div>
    </div>
  `;

	const inputPacienteId = $("#hfPacienteId");
	const hint = $("#hfHint");
	const list = $("#hfList");

	const prevBtn = $("#btnHfPrev");
	const prevBox = $("#hfPrevBox");
	let prevHistorico = null;

	const qp = qs("pacienteId");
	if (qp) inputPacienteId.value = qp;

	function buildTexto() {
		const v = (id) => String($(id)?.value || "").trim();
		return [
			`1) ${v("#hfQ1")}`,
			`2) ${v("#hfQ2")}`,
			`3) ${v("#hfQ3")}`,
			`4) ${v("#hfQ4")}`,
			`5) ${v("#hfQ5")}`,
			`6) ${v("#hfQ6")}`,
			`7) ${v("#hfQ7")}`,
			`8) ${v("#hfQ8")}`,
			`9) ${v("#hfQ9")}`,
			`10) ${v("#hfQ10")}`,
		].join("\n");
	}

	function renderPrevBox() {
		if (!prevBox) return;
		if (!prevHistorico) {
			prevBox.hidden = true;
			prevBox.innerHTML = "";
			return;
		}
		const versao = (prevHistorico.versao != null) ? `v${prevHistorico.versao}` : "—";
		const criado = prevHistorico.criadoEm ? new Date(prevHistorico.criadoEm).toLocaleString() : "";
		const textoHtml = historicoTextoParaHtml(prevHistorico.textoHistorico);

		prevBox.innerHTML = `
      <div class="item__top">
        <div>
          <p class="item__title">Histórico anterior ${escapeHTML(versao)}</p>
          <div class="item__meta">${criado ? escapeHTML(criado) : ""}</div>
        </div>
        <span class="badge">anterior</span>
      </div>
      <div class="hr"></div>
      <div>${textoHtml}</div>
    `;
	}

	async function carregarLista() {
		const pacienteId = Number(inputPacienteId.value);
		if (!pacienteId) {
			list.innerHTML = `<div class="muted">Paciente não definido.</div>`;
			return;
		}

		list.innerHTML = `<div class="muted">Carregando...</div>`;

		try {
			const historicos = await fetchHistoricosByPacienteId(pacienteId);

			prevHistorico = Array.isArray(historicos) && historicos.length >= 2 ? historicos[1] : null;
			if (prevBtn) prevBtn.disabled = !prevHistorico;
			if (prevBox && !prevBox.hidden) renderPrevBox();

			if (!Array.isArray(historicos) || historicos.length === 0) {
				list.innerHTML = `<div class="muted">Nenhuma versão ainda.</div>`;
				return;
			}

			list.innerHTML = historicos.map(h => {
				const versao = (h.versao != null) ? `v${h.versao}` : "—";
				const criado = h.criadoEm ? new Date(h.criadoEm).toLocaleString() : "";
				const textoHtml = historicoTextoParaHtml(h.textoHistorico);
				const anexos = Array.isArray(h.anexos) ? h.anexos : [];

				const anexosHtml = anexos.length ? anexos.map(a => {
					const nome = a.nomeOriginal || `anexo-${a.id}`;
					return `
            <div>
              <button class="btn btn--ghost" type="button" data-dl="${a.id}" data-name="${escapeAttr(nome)}">📎 ${escapeHTML(nome)}</button>
            </div>
          `;
				}).join("") : "";

				return `
          <div class="item">
            <div class="item__top">
              <div>
                <p class="item__title">Histórico ${escapeHTML(versao)}</p>
                <div class="item__meta">${criado ? escapeHTML(criado) : ""}</div>
              </div>
              <span class="badge">histórico</span>
            </div>
            <div class="hr"></div>
            <div>${textoHtml}</div>
            ${anexosHtml ? `<div class="hr"></div><div>${anexosHtml}</div>` : ""}
          </div>
        `;
			}).join("");

			$$('[data-dl]', list).forEach(btn => {
				btn.addEventListener('click', async () => {
					const anexoId = btn.dataset.dl;
					const nome = btn.dataset.name || "arquivo";
					try {
						await downloadHistoricoAnexo(anexoId, nome);
					} catch (err) {
						toast(inferFriendlyError(err));
					}
				});
			});

		} catch (err) {
			list.innerHTML = `<div class="muted">${escapeHTML(inferFriendlyError(err))}</div>`;
		}
	}

	if (isPaciente) hint.textContent = "Você está registrando seu histórico familiar. Ao salvar, uma nova versão é criada.";
	else if (isMedico) hint.textContent = "Visualização do histórico familiar do paciente.";
	else hint.textContent = "Acesso restrito.";

	(async () => {
		try {
			if (isPaciente) {
				const me = await fetchMePaciente();
				if (me?.idPaciente) inputPacienteId.value = String(me.idPaciente);
			}
		} catch (_) { }
		carregarLista().catch(() => { });
	})();

	prevBtn?.addEventListener("click", () => {
		if (!prevHistorico) return toast("Ainda não existe histórico anterior.");
		prevBox.hidden = !prevBox.hidden;
		if (!prevBox.hidden) renderPrevBox();
	});

	$("#formHistorico").addEventListener("submit", async (e) => {
		e.preventDefault();
		if (!isPaciente) return;

		const form = $("#formHistorico");
		if (!form.reportValidity()) return;

		const pacienteId = Number(inputPacienteId.value);
		if (!pacienteId) return toast("Não consegui identificar seu perfil de paciente.");

		const textoHistorico = buildTexto();

		try {
			await createHistoricoSnapshot({ pacienteId, textoHistorico });
			toast("Versão salva!");
			await carregarLista();
		} catch (err) {
			toast(inferFriendlyError(err));
		}
	});
}

/* -------- Exames -------- */
function renderExames() {
	const meta = getUserMeta();
	const role = normalizeRole(meta.role || "");
	if (!meta.token) {
		toast("Faça login para acessar.");
		return setTimeout(() => window.location.href = "index.html", 250);
	}
	if (role !== "PACIENTE") {
		toast("Acesso restrito.");
		return setTimeout(() => window.location.href = "index.html", 250);
	}

	const root = $("#pageRoot");
	root.innerHTML = `
    <div class="card card--flat">
      <div class="card__header">
        <h2 class="card__title">Exames e laudos</h2>
        <p class="card__subtitle">Anexe seus exames separadamente e descreva brevemente do que se trata.</p>
      </div>
      <div class="card__body">
        <div class="muted" id="eLoading">Carregando...</div>

        <div id="eBox" hidden>
          <form id="formExame" class="form">
            <input id="ePacienteId" type="hidden" />

            <div class="field">
              <label>Descrição do exame</label>
              <input id="eDescricao" maxlength="500"
                     placeholder="ex: Mamografia (jan/2026), Ultrassom, Laudo..." required />
              <div class="muted">Essa descrição aparece para o médico também.</div>
            </div>

            <div class="field">
              <label>Arquivo</label>
              <input id="eArquivo" type="file" accept="application/pdf,image/*" required />
              <div class="muted">PDF ou imagem.</div>
            </div>

            <div class="row">
              <button class="btn" type="submit">Enviar exame</button>
            </div>
          </form>

          <div class="hr"></div>
          <div class="form__section">
            <h3 class="section__title">Versões enviadas</h3>
            <div id="eList" class="list">
              <div class="muted">Carregando...</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;

	const inputPacienteId = $("#ePacienteId");
	const list = $("#eList");

	async function carregarLista() {
		const pacienteId = Number(inputPacienteId.value);
		if (!pacienteId) {
			list.innerHTML = `<div class="muted">Paciente não definido.</div>`;
			return;
		}

		list.innerHTML = `<div class="muted">Carregando...</div>`;
		try {
			const exames = await fetchExamesByPacienteId(pacienteId);
			if (!Array.isArray(exames) || exames.length === 0) {
				list.innerHTML = `<div class="muted">Nenhum exame enviado ainda.</div>`;
				return;
			}

			list.innerHTML = exames.map(e => {
				const versao = (e.versao != null) ? `v${e.versao}` : "—";
				const enviado = e.enviadoEm ? new Date(e.enviadoEm).toLocaleString() : "";
				const desc = String(e.descricao || "").trim();
				const nome = e.nomeOriginal || "arquivo";

				return `
          <div class="item">
            <div class="item__top">
              <div>
                <p class="item__title">Exame ${versao}</p>
                <div class="item__meta">${escapeHTML(enviado)}</div>
              </div>
              <span class="badge">exame</span>
            </div>

            ${desc ? `<div class="muted" style="margin-top:6px;">Descrição</div><div>${escapeHTML(desc)}</div>` : ""}

            <div class="hr"></div>
            <button class="btn btn--ghost" type="button"
                    data-dl-exame="${e.id}" data-name="${escapeAttr(nome)}">📎 ${escapeHTML(nome)}</button>
          </div>
        `;
			}).join("");

			$$('[data-dl-exame]', list).forEach(btn => {
				btn.addEventListener('click', async () => {
					const exameId = btn.dataset.dlExame;
					const nome = btn.dataset.name || "arquivo";
					try {
						await downloadExame(exameId, nome);
					} catch (err) {
						toast(inferFriendlyError(err));
					}
				});
			});

		} catch (err) {
			list.innerHTML = `<div class="muted">${escapeHTML(inferFriendlyError(err))}</div>`;
		}
	}

	(async () => {
		try {
			const me = await fetchMePaciente();
			if (!me?.idPaciente) throw new Error("Perfil de paciente não encontrado.");
			inputPacienteId.value = String(me.idPaciente);

			$("#eLoading").hidden = true;
			$("#eBox").hidden = false;

			await carregarLista();
		} catch (err) {
			$("#eLoading").textContent = inferFriendlyError(err);
		}
	})();

	$("#formExame").addEventListener("submit", async (e) => {
		e.preventDefault();

		const form = $("#formExame");
		if (!form.reportValidity()) return;

		const pacienteId = Number(inputPacienteId.value);
		const descricao = $("#eDescricao").value.trim();
		const file = $("#eArquivo").files?.[0];

		if (!pacienteId) return toast("Não consegui identificar seu perfil de paciente.");
		if (!file) return toast("Selecione um arquivo.");

		try {
			await uploadExame({ pacienteId, descricao, file });
			toast("Exame enviado!");

			$("#eDescricao").value = "";
			$("#eArquivo").value = "";

			await carregarLista();
		} catch (err) {
			toast(inferFriendlyError(err));
		}
	});
}

/* ---------------- Boot ---------------- */
(function boot() {
	const page = document.body.dataset.page || "home";
	const meta = getUserMeta();
	const logged = !!meta.token;
	const role = normalizeRole(meta.role || "");

	if (page === "cadastro" && logged) {
		goPanelByRole(role);
		return;
	}

	$("#app").innerHTML = buildLayout(page);
	bindCommon();

	if (page === "home") renderHome();
	if (page === "cadastro") renderCadastro();
	if (page === "paciente") renderPaciente();
	if (page === "medico") renderMedico();
	if (page === "medico_paciente") renderMedicoPaciente();
	if (page === "historico") renderHistorico();
	if (page === "exames") renderExames();
	if (page === "chat") renderChatHub();
})();
