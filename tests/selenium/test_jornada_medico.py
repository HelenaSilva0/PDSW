"""Jornada end-to-end do médico no Viva Mama.

O cenário cria uma paciente com histórico e exame, cria uma médica e percorre
o painel médico, filtros, prontuário compartilhado, documentos e conversa.

Execução:
    python -m unittest tests/selenium/test_jornada_medico.py -v

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
STEP_DELAY = float(os.getenv("SELENIUM_STEP_DELAY", "0"))
CHAT_PAUSE = float(os.getenv("SELENIUM_CHAT_PAUSE", "0"))


class JornadaMedicoTest(unittest.TestCase):
    """Valida as funcionalidades disponíveis para um médico."""

    def setUp(self) -> None:
        self.run_id = str(int(time.time() * 1000))
        self.patient_name = f"Beatriz Monteiro da Silva {self.run_id[-4:]}"
        self.patient_email = f"beatriz.{self.run_id}@exemplo.test"
        self.patient_phone = f"8197{self.run_id[-8:]}"
        self.doctor_name = "Dr. Rafael Mendes"
        self.doctor_email = f"rafael.{self.run_id}@exemplo.test"
        self.doctor_phone = f"8196{self.run_id[-8:]}"
        self.password = "VivaMama#2026"

        self.download_dir = Path(tempfile.mkdtemp(prefix="vivamama-download-"))
        self.upload_file = Path(tempfile.mktemp(prefix="ultrassom-", suffix=".pdf"))
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
            self.driver.save_screenshot(str(artifact_dir / f"medico-{self.run_id}.png"))
            self.driver.quit()
        if getattr(self, "upload_file", None):
            self.upload_file.unlink(missing_ok=True)
        if getattr(self, "download_dir", None):
            shutil.rmtree(self.download_dir, ignore_errors=True)

    def step(self, description: str) -> None:
        print(f"[MEDICO] {description}", flush=True)
        if STEP_DELAY:
            time.sleep(STEP_DELAY)

    @staticmethod
    def xpath_string(value: str) -> str:
        if "'" not in value:
            return f"'{value}'"
        return "concat(" + ", \"'\", ".join(f"'{part}'" for part in value.split("'")) + ")"

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
        field = self.wait.until(EC.visibility_of_element_located((By.NAME, name)))
        field.clear()
        field.send_keys(value)

    def fill_date(self, name: str, iso_value: str) -> None:
        field = self.wait.until(EC.visibility_of_element_located((By.NAME, name)))
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

    def register_account(self, role: str) -> None:
        self.driver.get(BASE_URL)
        self.click((By.LINK_TEXT, "Criar conta"))
        self.wait_url("/cadastro")
        if role == "MEDICO":
            self.click((By.XPATH, "//button[contains(., 'médico') or contains(., 'mÃ©dico')]") )
        self.fill("nome", self.patient_name if role == "PACIENTE" else self.doctor_name)
        self.fill("email", self.patient_email if role == "PACIENTE" else self.doctor_email)
        self.fill("telefone", self.patient_phone if role == "PACIENTE" else self.doctor_phone)
        self.fill("senha", self.password)
        if role == "PACIENTE":
            self.fill_date("dataNascimento", "1984-03-22")
            Select(self.driver.find_element(By.NAME, "genero")).select_by_value("F")
            self.fill(
                "observacoes",
                "Histórico materno de câncer de mama; acompanhamento preventivo semestral.",
            )
        else:
            self.fill("crm", f"CRM-PE-{self.run_id[-6:]}")
            self.fill("especialidade", "Ginecologia e Mastologia")
        self.click((By.XPATH, "//form//button[@type='submit' and contains(., 'Criar conta')]") )
        self.wait_url("/login")

    def login(self, email: str) -> None:
        self.fill("email", email)
        self.fill("senha", self.password)
        self.click((By.XPATH, "//form//button[@type='submit' and contains(., 'Entrar')]") )
        self.wait.until(EC.any_of(EC.url_contains("/paciente"), EC.url_contains("/medico")))

    def logout(self) -> None:
        self.click((By.XPATH, "//header//button[contains(., 'Sair')]") )
        self.wait.until(
            lambda driver: driver.current_url in {f"{BASE_URL}/", f"{BASE_URL}/login"}
        )

    def fill_history(self) -> None:
        answers = [
            "Sim. Minha mãe recebeu diagnóstico de câncer de mama aos 58 anos.",
            "Aproximadamente 58 anos.",
            "Não há outros tipos de câncer conhecidos na família.",
            "Minha mãe realizou painel genético; resultado negativo para BRCA1 e BRCA2.",
            "Não conheço casos de câncer de mama em homens.",
            "Março de 2026, ultrassonografia de rotina.",
            "Nódulo benigno acompanhado por ultrassonografia em 2023; sem cirurgia.",
            "Anticoncepcional por cinco anos; nunca usei reposição hormonal.",
            "Duas gestações, com amamentação por 11 e 16 meses.",
            "Hipertensão leve, controlada com losartana.",
        ]
        self.wait.until(
            lambda driver: len(driver.find_elements(By.CSS_SELECTOR, ".question-grid input")) == 9
        )
        textarea = self.driver.find_element(By.CSS_SELECTOR, ".question-grid textarea")
        textarea.send_keys(answers[0])
        for field, answer in zip(
            self.driver.find_elements(By.CSS_SELECTOR, ".question-grid input"), answers[1:]
        ):
            field.send_keys(answer)

    def seed_patient_information(self) -> None:
        self.step("criando paciente e dados clínicos compartilháveis")
        self.register_account("PACIENTE")
        self.login(self.patient_email)

        self.click((By.XPATH, "//header//a[contains(., 'Histórico')]") )
        self.wait_url("/paciente/historico-familiar")
        self.fill_history()
        self.driver.find_element(By.CSS_SELECTOR, "input[type='file']").send_keys(str(self.upload_file))
        self.click((By.XPATH, "//button[contains(., 'Salvar nova versão') or contains(., 'Salvar nova versÃ£o')]") )
        self.wait_text("Versão 1")

        self.click((By.XPATH, "//header//a[contains(., 'Exames')]") )
        self.wait_url("/paciente/exames")
        exam_description = self.wait.until(
            EC.visibility_of_element_located((By.CSS_SELECTOR, ".form textarea"))
        )
        exam_description.send_keys("Ultrassonografia mamária bilateral - março de 2026")
        self.driver.find_element(By.CSS_SELECTOR, "input[type='file']").send_keys(str(self.upload_file))
        self.click((By.XPATH, "//button[contains(., 'Enviar exame')]") )
        self.wait_text("Ultrassonografia mamária bilateral - março de 2026")
        self.logout()

    def click_patient_card(self) -> None:
        def find_card(driver):
            for card in driver.find_elements(By.CSS_SELECTOR, ".patient-list a"):
                if self.patient_name in card.text:
                    return card
            return False

        self.wait.until(find_card).click()

    def select_dashboard_filter(self, label: str, visible_text: str) -> None:
        select = self.wait.until(
            EC.visibility_of_element_located(
                (
                    By.XPATH,
                    f"//label[.//span[contains(., {self.xpath_string(label)})]]/select",
                )
            )
        )
        Select(select).select_by_visible_text(visible_text)

    def send_chat_message(self, message: str) -> None:
        field = self.wait.until(EC.visibility_of_element_located((By.ID, "message")))
        field.send_keys(message)
        self.click((By.XPATH, "//form[contains(@class, 'message-form')]//button[contains(., 'Enviar')]") )
        self.wait.until(
            EC.presence_of_element_located(
                (By.XPATH, f"//div[contains(@class, 'messages')]//*[contains(., {self.xpath_string(message)})]")
            )
        )
        if CHAT_PAUSE:
            print(f"[MEDICO] chat visível por {CHAT_PAUSE:g}s", flush=True)
            time.sleep(CHAT_PAUSE)

    def test_jornada_completa_do_medico(self) -> None:
        self.step("iniciando a jornada")
        self.seed_patient_information()

        self.step("criando e acessando a conta médica")
        self.register_account("MEDICO")
        self.login(self.doctor_email)
        self.wait_text("Área médica")
        self.wait_text("Pacientes")

        self.step("pesquisando e filtrando pacientes")
        search = self.driver.find_element(By.CSS_SELECTOR, "input[type='search']")
        search.send_keys(self.patient_name)
        self.wait_text("1 resultado")
        self.select_dashboard_filter("Gênero", "Feminino")
        self.select_dashboard_filter("Idade", "30 a 49")
        self.select_dashboard_filter("Ordenar", "Mais exames")
        self.click_patient_card()

        self.step("consultando a visão geral do prontuário")
        self.wait_text(self.patient_name)
        self.wait_text("Histórico materno de câncer de mama")
        self.wait_text("Acesso responsável")

        self.step("consultando o histórico familiar em modo somente leitura")
        self.click((By.XPATH, "//nav[@aria-label='Dados do paciente']//a[contains(., 'Histórico familiar')]") )
        self.wait_url("/historico")
        self.wait_text("Versão 1")
        self.wait_text("Minha mãe recebeu diagnóstico")

        self.step("consultando e baixando o exame compartilhado")
        self.click((By.XPATH, "//nav[@aria-label='Dados do paciente']//a[contains(., 'Exames')]") )
        self.wait_url("/exames")
        self.wait_text("Ultrassonografia mamária bilateral - março de 2026")
        self.click((By.CSS_SELECTOR, ".stack .card button"))
        self.wait_for_download()

        self.step("iniciando conversa com a paciente")
        self.click((By.XPATH, "//nav[@aria-label='Dados do paciente']//a[contains(., 'Chat')]") )
        self.wait_url("/chat")
        self.send_chat_message(
            "Olá, Beatriz. Analisei seu histórico e o exame compartilhado. Vamos manter o acompanhamento."
        )

        self.step("validando a lista geral de conversas do médico")
        self.click((By.XPATH, "//header//a[contains(., 'Chat')]") )
        self.wait_url("/medico/chat")
        self.wait_text(self.patient_name)
        self.wait_text("Olá, Beatriz")
        self.step("jornada do médico concluída com sucesso")


if __name__ == "__main__":
    unittest.main(verbosity=2)
