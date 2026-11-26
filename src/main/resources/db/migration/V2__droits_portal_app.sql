-- Ajout seul garanti par la base : l'application peut écrire et relire le journal, jamais le modifier ni le supprimer.
GRANT SELECT, INSERT ON AUDIT_EVENT TO PORTAL_APP;
