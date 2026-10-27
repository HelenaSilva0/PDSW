-- Aplicar com backup prÃ©vio. Para bancos existentes, executar esta migraÃ§Ã£o
-- antes de ativar JPA ddl-auto=validate.
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20);
UPDATE users SET status = CASE WHEN role = 'MEDICO' THEN 'PENDENTE' ELSE 'ATIVO' END WHERE status IS NULL;
ALTER TABLE users ALTER COLUMN status SET NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS status_alterado_por_user_id INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS status_alterado_em TIMESTAMP WITH TIME ZONE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_chat_medico_paciente ON chat (medico_id, paciente_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_exame_paciente_versao ON exame (paciente_id, versao);
CREATE UNIQUE INDEX IF NOT EXISTS uk_historico_paciente_versao ON historico_familiar (paciente_id, versao);
CREATE INDEX IF NOT EXISTS idx_exame_paciente_data ON exame (paciente_id, enviado_em);
CREATE INDEX IF NOT EXISTS idx_historico_paciente_data ON historico_familiar (paciente_id, criado_em);
