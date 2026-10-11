-- Kustutab insultbot schema (mis põhimõtteliselt kustutab kõik tabelid)
DROP SCHEMA IF EXISTS insultbot CASCADE;
-- Loob uue insultbot schema vajalikud õigused
CREATE SCHEMA insultbot
-- taastab vajalikud andmebaasi õigused
    GRANT ALL ON SCHEMA insultbot TO insultbot;
GRANT ALL ON SCHEMA insultbot TO PUBLIC;