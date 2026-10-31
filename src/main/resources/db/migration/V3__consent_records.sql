-- Registra a versão, data e contexto dos aceites feitos no cadastro.
ALTER TABLE users ADD COLUMN IF NOT EXISTS termos_versao VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS termos_aceitos_em TIMESTAMP WITH TIME ZONE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS privacidade_versao VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS privacidade_ciente_em TIMESTAMP WITH TIME ZONE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS dados_saude_consentimento_em TIMESTAMP WITH TIME ZONE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS confidencialidade_aceita_em TIMESTAMP WITH TIME ZONE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS consentimento_ip VARCHAR(45);
ALTER TABLE users ADD COLUMN IF NOT EXISTS consentimento_user_agent VARCHAR(500);
