"""Jornada end-to-end do paciente no Viva Mama.

Pré-requisitos:
    - backend em http://localhost:8080;
    - frontend em http://localhost:5173;
    - Python com selenium instalado e Chrome disponível.

Execução:
    python -m unittest tests/selenium/test_jornada_paciente.py -v

Use HEADLESS=0 para acompanhar a execução visualmente.
"""

from __future__ import annotations

import os
import shutil
import tempfile
import time
import unittest
from pathlib import Path

from selenium import webdriver
from selenium.common.exceptions import ElementClickInterceptedException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select, WebDriverWait


BASE_URL = os.getenv("VIVAMAMA_URL", "http://localhost:5173").rstrip("/")
WAIT_SECONDS = int(os.getenv("SELENIUM_WAIT", "25"))


class JornadaPacienteTest(unittest.TestCase):
    """Executa o fluxo completo de uma paciente e valida cada funcionalidade."""

    def setUp(self) -> None:
        self.run_id = str(int(time.time() * 1000))
        self.patient_name = f"Mariana Alves de Souza {self.run_id}"
        self.patient_email = f"mariana.{self.run_id}@exemplo.test"
        self.patient_phone = f"8199{self.run_id[-8:]}"
        self.doctor_name = "Dra. Camila Nogueira"
        self.doctor_email = f"camila.{self.run_id}@exemplo.test"
        self.doctor_phone = f"8198{self.run_id[-8:]}"
        self.password = "VivaMama#2026"

        self.download_dir = Path(tempfile.mkdtemp(prefix="vivamama-download-"))
        self.upload_file = Path(tempfile.mktemp(prefix="mamografia-", suffix=".pdf"))
        self.upload_file.write_bytes(
            b"%PDF-1.4\n% Viva Mama fixture\n1 0 obj<<>>endobj\n"
        )

        options = webdriver.ChromeOptions()
        if os.getenv("HEADLESS", "1").lower() not in {"0", "false", "no"}:
            options.add_argument("--headless=new")
        options.add_argument("--window-size=1440,1100")
        options.add_argument("--disable-gpu")
        options.add_experimental_option(
            "prefs",
            {
                "download.default_directory": str(self.download_dir),
                "download.prompt_for_download": False,
                "download.directory_upgrade": True,
                "plugins.always_open_pdf_externally": True,
            },
        )
        self.driver = webdriver.Chrome(options=options)
        self.wait = WebDriverWait(self.driver, WAIT_SECONDS)

    def tearDown(self) -> None:
        if getattr(self, "driver", None):
            artifact_dir = Path("test-artifacts") / "selenium"
            artifact_dir.mkdir(parents=True, exist_ok=True)
            self.driver.save_screenshot(str(artifact_dir / f"paciente-{self.run_id}.png"))
            self.driver.quit()
        if getattr(self, "upload_file", None):
            self.upload_file.unlink(missing_ok=True)
        if getattr(self, "download_dir", None):
            shutil.rmtree(self.download_dir, ignore_errors=True)

    def step(self, description: str) -> None:
        print(f"[PACIENTE] {description}", flush=True)

    def by_name(self, name: str):
        return (By.NAME, name)

    def click(self, locator) -> None:
        element = self.wait.until(EC.element_to_be_clickable(locator))
        self.driver.execute_script(
            "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
            element,
        )
        try:
            element.click()
        except ElementClickInterceptedException:
            self.driver.execute_script("arguments[0].click();", element)

    def fill(self, name: str, value: str) -> None:
        field = self.wait.until(EC.visibility_of_element_located(self.by_name(name)))
        field.clear()
        field.send_keys(value)

    def fill_date(self, name: str, iso_value: str) -> None:
        field = self.wait.until(EC.visibility_of_element_located(self.by_name(name)))
        field.clear()
        field.send_keys(iso_value)
        if field.get_attribute("value") != iso_value:
            self.driver.execute_script(
                """
                const field = arguments[0];
                const value = arguments[1];
                const setter = Object.getOwnPropertyDescriptor(
                    HTMLInputElement.prototype, 'value'
                ).set;
                setter.call(field, value);
                field.dispatchEvent(new Event('input', {bubbles: true}));
                field.dispatchEvent(new Event('change', {bubbles: true}));
                """,
                field,
                iso_value,
            )

    def wait_text(self, text: str):
        return self.wait.until(
            EC.presence_of_element_located(
                (By.XPATH, f"//*[contains(normalize-space(.), {self.xpath_string(text)})]")
            )
        )

    def wait_url(self, fragment: str) -> None:
        self.wait.until(EC.url_contains(fragment))

    def wait_for_download(self, before: set[Path] | None = None) -> None:
        before = before or set()
        self.wait.until(
            lambda _driver: any(
                file not in before
                and
                file.is_file()
                and not file.name.endswith((".crdownload", ".tmp"))
                for file in self.download_dir.iterdir()
            )
        )

    @staticmethod
    def xpath_string(value: str) -> str:
        if "'" not in value:
            return f"'{value}'"
        return "concat(" + ", \"'\", ".join(f"'{part}'" for part in value.split("'")) + ")"

    def click_header_link(self, text: str) -> None:
        self.click((By.XPATH, f"//header//a[contains(., {self.xpath_string(text)})]"))

    def register_patient(self) -> None:
        self.step("abrindo o cadastro de paciente")
        self.driver.get(BASE_URL)
        self.click((By.LINK_TEXT, "Criar conta"))
        self.wait_url("/cadastro")
        self.fill("nome", self.patient_name)
        self.fill("email", self.patient_email)
        self.fill("telefone", self.patient_phone)
        self.fill("senha", self.password)
        self.fill_date("dataNascimento", "1989-07-14")
        self.click(self.by_name("genero"))
        Select(self.driver.find_element(*self.by_name("genero"))).select_by_value("F")
        self.fill(
            "observacoes",
            "Acompanhamento preventivo anual; sem sintomas atuais; mãe com diagnóstico aos 62 anos.",
        )
        self.click((By.XPATH, "//form//button[@type='submit' and contains(., 'Criar conta')]") )
        self.wait_url("/login")

    def login(self, email: str) -> None:
        self.step(f"entrando com {email}")
        self.fill("email", email)
        self.fill("senha", self.password)
        self.click((By.XPATH, "//form//button[@type='submit' and contains(., 'Entrar')]") )
        self.wait.until(EC.any_of(EC.url_contains("/paciente"), EC.url_contains("/medico")))

    def logout(self) -> None:
        self.click((By.XPATH, "//header//button[contains(., 'Sair')]") )
        self.wait.until(
            lambda driver: driver.current_url in {f"{BASE_URL}/", f"{BASE_URL}/login"}
        )

    def fill_history(self, answers: list[str]) -> None:
        self.wait.until(
            lambda driver: len(driver.find_elements(By.CSS_SELECTOR, ".question-grid input")) == 9
        )
        textarea = self.driver.find_element(By.CSS_SELECTOR, ".question-grid textarea")
        textarea.clear()
        textarea.send_keys(answers[0])
        for field, answer in zip(
            self.driver.find_elements(By.CSS_SELECTOR, ".question-grid input"), answers[1:]
        ):
            field.clear()
            field.send_keys(answer)

    def click_patient_card(self) -> None:
        def find_card(driver):
            for card in driver.find_elements(By.CSS_SELECTOR, ".patient-list a"):
                if self.patient_name in card.text:
                    return card
            return False

        self.wait.until(find_card).click()

    def send_chat_message(self, message: str) -> None:
        field = self.wait.until(EC.visibility_of_element_located((By.ID, "message")))
        field.send_keys(message)
        self.click((By.XPATH, "//form[contains(@class, 'message-form')]//button[contains(., 'Enviar')]") )
        self.wait.until(
            EC.presence_of_element_located(
                (By.XPATH, f"//div[contains(@class, 'messages')]//*[contains(., {self.xpath_string(message)})]")
            )
        )

    def prepare_doctor_chat(self) -> None:
        """Cria o profissional e a conversa para o fluxo do paciente ser completo."""
        self.step("criando uma profissional para habilitar a conversa")
        self.logout()
        self.click((By.LINK_TEXT, "Criar conta"))
        self.wait_url("/cadastro")
        self.click((By.XPATH, "//button[contains(., 'médico') or contains(., 'mÃ©dico')]") )
        self.fill("nome", self.doctor_name)
        self.fill("email", self.doctor_email)
        self.fill("telefone", self.doctor_phone)
        self.fill("senha", self.password)
        self.fill("crm", f"CRM-PE-{self.run_id[-6:]}")
        self.fill("especialidade", "Mastologia")
        self.click((By.XPATH, "//form//button[@type='submit' and contains(., 'Criar conta')]") )
        self.wait_url("/login")
        self.login(self.doctor_email)

        self.step("localizando a paciente no painel médico")
        self.wait_text("Pacientes")
        search = self.wait.until(
            EC.visibility_of_element_located((By.CSS_SELECTOR, "input[type='search']"))
        )
        search.send_keys(self.patient_name)
        self.click_patient_card()
        patient_url = self.driver.current_url
        self.click((By.XPATH, "//button[contains(., 'Abrir conversa')]") )
        self.wait_url("/chat")
        self.send_chat_message(
            "Olá, Mariana. Recebi seus dados e vou revisar o exame antes do nosso retorno."
        )

        self.step("confirmando que o médico consegue consultar histórico e exames")
        self.driver.get(patient_url + "/historico")
        self.wait_text("Versão 2")
        self.driver.get(patient_url + "/exames")
        self.wait_text("Mamografia bilateral de rastreamento")

    def test_jornada_completa_do_paciente(self) -> None:
        self.step("iniciando a jornada")
        self.register_patient()
        self.login(self.patient_email)
        self.wait_text("Área da paciente")

        self.step("atualizando dados pessoais e observações clínicas")
        self.fill("nome", self.patient_name)
        self.fill_date("dataNascimento", "1989-07-14")
        self.click(self.by_name("genero"))
        Select(self.driver.find_element(*self.by_name("genero"))).select_by_value("F")
        self.fill(
            "observacoes",
            "Acompanhamento preventivo anual; mãe com diagnóstico aos 62 anos; sem sintomas atuais.",
        )
        self.click((By.XPATH, "//form//button[contains(., 'Salvar alterações') or contains(., 'Salvar alteraÃ§Ãµes')]") )
        self.wait_text("Dados atualizados")

        self.step("respondendo ao histórico familiar e anexando comprovante")
        self.click_header_link("Histórico")
        self.wait_url("/paciente/historico-familiar")
        first_answers = [
            "Sim. Minha mãe teve câncer de mama aos 62 anos.",
            "Aproximadamente 62 anos.",
            "Sim. Um tio teve câncer de próstata aos 70 anos.",
            "Não realizou teste genético até o momento.",
            "Não conheço casos de câncer de mama masculino.",
            "Julho de 2025, mamografia sem alterações relevantes.",
            "Nunca tive nódulos, biópsias ou cirurgias nas mamas.",
            "Usei anticoncepcional oral por cerca de oito anos; não faço reposição hormonal.",
            "Uma gestação em 2018 e amamentação por 14 meses.",
            "Hipotireoidismo controlado com levotiroxina.",
        ]
        self.fill_history(first_answers)
        self.driver.find_element(By.CSS_SELECTOR, "input[type='file']").send_keys(str(self.upload_file))
        self.click((By.XPATH, "//button[contains(., 'Salvar nova versão') or contains(., 'Salvar nova versÃ£o')]") )
        self.wait_text("Versão 1")
        self.click((By.CSS_SELECTOR, ".attachments button"))
        self.wait_for_download()

        self.step("criando uma segunda versão para validar o versionamento")
        second_answers = first_answers.copy()
        second_answers[5] = "Agosto de 2026, mamografia bilateral solicitada no acompanhamento anual."
        self.fill_history(second_answers)
        self.click((By.XPATH, "//button[contains(., 'Salvar nova versão') or contains(., 'Salvar nova versÃ£o')]") )
        self.wait_text("Versão 2")

        self.step("enviando o exame de mamografia")
        self.click_header_link("Exames")
        self.wait_url("/paciente/exames")
        description = self.wait.until(
            EC.visibility_of_element_located((By.CSS_SELECTOR, ".form textarea"))
        )
        description.send_keys("Mamografia bilateral de rastreamento")
        self.driver.find_element(By.CSS_SELECTOR, "input[type='file']").send_keys(str(self.upload_file))
        self.click((By.XPATH, "//button[contains(., 'Enviar exame')]") )
        self.wait_text("Mamografia bilateral de rastreamento")
        before_download = set(self.download_dir.iterdir())
        self.click((By.CSS_SELECTOR, ".stack .card button"))
        self.wait_for_download(before_download)

        self.prepare_doctor_chat()

        self.step("retornando ao perfil da paciente e respondendo no chat")
        self.logout()
        self.login(self.patient_email)
        self.click_header_link("Chat")
        self.wait_url("/paciente/chat")
        self.wait_text("Olá, Mariana")
        self.send_chat_message(
            "Obrigada, doutora. Vou manter o acompanhamento anual e aguardo suas orientações."
        )
        self.wait_text("Obrigada, doutora")
        self.step("jornada do paciente concluída com sucesso")


if __name__ == "__main__":
    unittest.main(verbosity=2)
