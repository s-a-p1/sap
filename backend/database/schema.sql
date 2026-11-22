
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `analise_desempenho`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `analise_desempenho` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `acertos` int NOT NULL,
  `total_questoes` int NOT NULL,
  `percentual` double NOT NULL,
  `dificuldade` varchar(255) DEFAULT NULL,
  `prioridade` varchar(255) DEFAULT NULL,
  `recomendacao` varchar(255) NOT NULL,
  `resultado_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1nqdc3sod44rkdd3ut37geed1` (`resultado_id`),
  CONSTRAINT `FK70wmb01mjawc9h8gfj2omdi05` FOREIGN KEY (`resultado_id`) REFERENCES `resultados` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `assuntos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assuntos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descricao` varchar(255) DEFAULT NULL,
  `nome` varchar(100) NOT NULL,
  `disciplina_id` bigint NOT NULL,
  `visivel_catalogo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `FKrywo4e4myklpl695fg2hmhha3` (`disciplina_id`),
  CONSTRAINT `FKrywo4e4myklpl695fg2hmhha3` FOREIGN KEY (`disciplina_id`) REFERENCES `disciplinas` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=203 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `avaliacoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `avaliacoes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `titulo` varchar(150) NOT NULL,
  `assunto_id` bigint NOT NULL,
  `data_criacao` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkb2xm5h413s6unuxnx1ojn45e` (`assunto_id`),
  CONSTRAINT `FKkb2xm5h413s6unuxnx1ojn45e` FOREIGN KEY (`assunto_id`) REFERENCES `assuntos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `configuracoes_academicas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuracoes_academicas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `curso_id` bigint NOT NULL,
  `semestre` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configuracao_usuario` (`usuario_id`),
  KEY `fk_configuracao_curso` (`curso_id`),
  CONSTRAINT `fk_configuracao_curso` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`),
  CONSTRAINT `fk_configuracao_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `cursos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cursos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nome` varchar(150) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cursos_nome` (`nome`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `desempenhos_assunto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `desempenhos_assunto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resultado_id` bigint NOT NULL,
  `assunto_id` bigint NOT NULL,
  `total_questoes` int NOT NULL,
  `acertos` int NOT NULL,
  `erros` int NOT NULL,
  `percentual` decimal(5,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_desempenho_assunto_resultado` (`resultado_id`),
  KEY `fk_desempenho_assunto_assunto` (`assunto_id`),
  CONSTRAINT `fk_desempenho_assunto_assunto` FOREIGN KEY (`assunto_id`) REFERENCES `assuntos` (`id`),
  CONSTRAINT `fk_desempenho_assunto_resultado` FOREIGN KEY (`resultado_id`) REFERENCES `resultados` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `disciplinas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `disciplinas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descricao` varchar(255) DEFAULT NULL,
  `nome` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `logs_auditoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logs_auditoria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `acao` varchar(50) NOT NULL,
  `descricao` varchar(500) NOT NULL,
  `data_hora` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=617 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `matriculas_disciplinas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `matriculas_disciplinas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `configuracao_id` bigint NOT NULL,
  `oferta_disciplina_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_matricula_configuracao_oferta` (`configuracao_id`,`oferta_disciplina_id`),
  KEY `fk_matricula_oferta` (`oferta_disciplina_id`),
  CONSTRAINT `fk_matricula_configuracao` FOREIGN KEY (`configuracao_id`) REFERENCES `configuracoes_academicas` (`id`),
  CONSTRAINT `fk_matricula_oferta` FOREIGN KEY (`oferta_disciplina_id`) REFERENCES `ofertas_disciplinas` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=234 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `metas_estudo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `metas_estudo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `assunto_id` bigint NOT NULL,
  `titulo` varchar(200) NOT NULL,
  `prioridade` varchar(20) NOT NULL,
  `data_prevista` date NOT NULL,
  `concluida` bit(1) NOT NULL,
  `origem` varchar(20) NOT NULL,
  `resultado_origem_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_metas_estudo_assunto` (`assunto_id`),
  KEY `fk_metas_estudo_resultado_origem` (`resultado_origem_id`),
  CONSTRAINT `fk_metas_estudo_assunto` FOREIGN KEY (`assunto_id`) REFERENCES `assuntos` (`id`),
  CONSTRAINT `fk_metas_estudo_resultado_origem` FOREIGN KEY (`resultado_origem_id`) REFERENCES `resultados` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `notificacoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacoes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `titulo` varchar(150) NOT NULL,
  `mensagem` varchar(1000) NOT NULL,
  `categoria` varchar(30) NOT NULL,
  `lida` tinyint(1) NOT NULL DEFAULT '0',
  `data_criacao` datetime(6) NOT NULL,
  `referencia_tipo` varchar(50) DEFAULT NULL,
  `referencia_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_notificacoes_usuario` (`usuario_id`),
  CONSTRAINT `fk_notificacoes_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=122 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ofertas_disciplinas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ofertas_disciplinas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `curso_id` bigint NOT NULL,
  `semestre` int NOT NULL,
  `disciplina_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oferta_curso_semestre_disciplina` (`curso_id`,`semestre`,`disciplina_id`),
  KEY `fk_oferta_disciplina` (`disciplina_id`),
  CONSTRAINT `fk_oferta_curso` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`),
  CONSTRAINT `fk_oferta_disciplina` FOREIGN KEY (`disciplina_id`) REFERENCES `disciplinas` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=181 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `preferencias_notificacoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `preferencias_notificacoes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `notificacoes_ativas` tinyint(1) NOT NULL DEFAULT '1',
  `lembretes_plano` tinyint(1) NOT NULL DEFAULT '1',
  `metas_pendentes` tinyint(1) NOT NULL DEFAULT '1',
  `recomendacoes_revisao` tinyint(1) NOT NULL DEFAULT '1',
  `avaliacoes_proximas` tinyint(1) NOT NULL DEFAULT '0',
  `evolucao_desempenho` tinyint(1) NOT NULL DEFAULT '1',
  `resumo_semanal` tinyint(1) NOT NULL DEFAULT '0',
  `antecedencia_lembretes` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_preferencias_usuario` (`usuario_id`),
  CONSTRAINT `fk_preferencias_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `questoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `questoes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `alternativa_a` varchar(255) NOT NULL,
  `alternativa_b` varchar(255) NOT NULL,
  `alternativa_c` varchar(255) NOT NULL,
  `alternativa_d` varchar(255) NOT NULL,
  `enunciado` text NOT NULL,
  `resposta_correta` varchar(1) NOT NULL,
  `avaliacao_id` bigint NOT NULL,
  `assunto_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9blmg0aoq4pusb7dvmmfu3qgu` (`avaliacao_id`),
  CONSTRAINT `FK9blmg0aoq4pusb7dvmmfu3qgu` FOREIGN KEY (`avaliacao_id`) REFERENCES `avaliacoes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=534 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `resultados`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resultados` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `acertos` int NOT NULL,
  `data_realizacao` datetime(6) NOT NULL,
  `erros` int NOT NULL,
  `percentual` decimal(5,2) NOT NULL,
  `total_questoes` int NOT NULL,
  `usuario_id` bigint NOT NULL,
  `avaliacao_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjfhsys6ojts8ukm1xxa6xdqv` (`avaliacao_id`),
  CONSTRAINT `FKjfhsys6ojts8ukm1xxa6xdqv` FOREIGN KEY (`avaliacao_id`) REFERENCES `avaliacoes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `semestres_cursos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `semestres_cursos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `curso_id` bigint NOT NULL,
  `numero` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_semestre_curso` (`curso_id`,`numero`),
  CONSTRAINT `fk_semestre_curso_curso` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tokens_recuperacao_senha`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tokens_recuperacao_senha` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `token_hash` varchar(64) NOT NULL,
  `data_expiracao` datetime(6) NOT NULL,
  `data_utilizacao` datetime(6) DEFAULT NULL,
  `data_criacao` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token_recuperacao_hash` (`token_hash`),
  KEY `idx_token_recuperacao_usuario` (`usuario_id`),
  CONSTRAINT `fk_token_recuperacao_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ativo` bit(1) NOT NULL,
  `email` varchar(150) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `provedor` enum('GITHUB','GOOGLE','LOCAL') NOT NULL,
  `provedor_id` varchar(255) DEFAULT NULL,
  `senha` varchar(255) DEFAULT NULL,
  `tipo` varchar(20) NOT NULL,
  `aceitou_termos` tinyint(1) NOT NULL DEFAULT '0',
  `data_aceite_termos` datetime DEFAULT NULL,
  `versao_termos` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkfsp0s1tflm1cwlj8idhqsad0` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

