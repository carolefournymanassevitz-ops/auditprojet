#!/bin/bash
# Exécuté une seule fois, au premier démarrage du conteneur Oracle Free.
# - CENTRAL_SIM : schéma fictif qui joue le rôle du système central (référentiel compteur), sans compte de connexion.
# - PORTAL      : propriétaire du schéma du portail ; seul Flyway s'y connecte.
# - PORTAL_APP  : compte de l'application ; il ne reçoit que SELECT et INSERT sur le journal (migration V2).
# Les mots de passe viennent des variables d'environnement, jamais du dépôt.

set -e

sqlplus -s / as sysdba <<EOF
WHENEVER SQLERROR EXIT SQL.SQLCODE
ALTER SESSION SET CONTAINER = FREEPDB1;

CREATE USER portal IDENTIFIED BY "${PORTAL_PASSWORD}" QUOTA UNLIMITED ON USERS;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO portal;

CREATE USER portal_app IDENTIFIED BY "${PORTAL_APP_PASSWORD}";
GRANT CREATE SESSION TO portal_app;

CREATE USER central_sim NO AUTHENTICATION QUOTA UNLIMITED ON USERS;
CREATE TABLE central_sim.meter (
    prm   VARCHAR2(20) PRIMARY KEY,
    owner VARCHAR2(30) NOT NULL
);
INSERT INTO central_sim.meter VALUES ('30001000000001', 'ELD_A');
INSERT INTO central_sim.meter VALUES ('30001000000002', 'ELD_A');
INSERT INTO central_sim.meter VALUES ('30001000000009', 'ELD_A');
INSERT INTO central_sim.meter VALUES ('30002000000001', 'ELD_B');
COMMIT;

-- Droit accordé directement, pas par un rôle : c'est ce qu'exigerait une vue PORTAL sur le schéma central
-- (§7.2 : lecture des données du système central par des vues).
GRANT SELECT ON central_sim.meter TO portal_app;
EXIT;
EOF
