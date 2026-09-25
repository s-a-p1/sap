import { useEffect, useState } from "react";
import "./App.css";

const API_URL = "http://localhost:8080";
const AVALIACAO_ID = 1;

function GoogleIcon() {
  return (
    <svg
      className="oauth-icon"
      viewBox="0 0 24 24"
      aria-hidden="true"
    >
      <path
        fill="#4285F4"
        d="M21.35 12.22c0-.74-.07-1.45-.19-2.13H12v4.03h5.24a4.48 4.48 0 0 1-1.94 2.94v2.62h3.14c1.84-1.69 2.91-4.19 2.91-7.46Z"
      />
      <path
        fill="#34A853"
        d="M12 21.75c2.62 0 4.82-.87 6.43-2.36l-3.14-2.62c-.87.58-1.98.93-3.29.93-2.53 0-4.67-1.71-5.44-4.01H3.32v2.7A9.72 9.72 0 0 0 12 21.75Z"
      />
      <path
        fill="#FBBC05"
        d="M6.56 13.69A5.85 5.85 0 0 1 6.25 12c0-.59.1-1.16.31-1.69v-2.7H3.32A9.73 9.73 0 0 0 2.25 12c0 1.57.38 3.05 1.07 4.39l3.24-2.7Z"
      />
      <path
        fill="#EA4335"
        d="M12 6.3c1.43 0 2.71.49 3.72 1.45l2.79-2.79C16.82 3.39 14.62 2.25 12 2.25a9.72 9.72 0 0 0-8.68 5.36l3.24 2.7C7.33 8.01 9.47 6.3 12 6.3Z"
      />
    </svg>
  );
}

function GitHubIcon() {
  return (
    <svg
      className="oauth-icon github-svg"
      viewBox="0 0 24 24"
      aria-hidden="true"
    >
      <path
        fill="currentColor"
        d="M12 .7a11.5 11.5 0 0 0-3.64 22.41c.58.11.79-.25.79-.56v-2.23c-3.22.7-3.9-1.37-3.9-1.37-.53-1.34-1.29-1.7-1.29-1.7-1.05-.72.08-.71.08-.71 1.17.08 1.78 1.2 1.78 1.2 1.04 1.78 2.72 1.27 3.38.97.1-.75.41-1.27.74-1.56-2.57-.29-5.27-1.29-5.27-5.69 0-1.26.45-2.29 1.19-3.09-.12-.29-.52-1.47.11-3.05 0 0 .97-.31 3.16 1.18A10.98 10.98 0 0 1 12 6.11c.98 0 1.94.13 2.85.38 2.2-1.49 3.16-1.18 3.16-1.18.63 1.58.23 2.76.11 3.05.74.8 1.19 1.83 1.19 3.09 0 4.42-2.71 5.39-5.29 5.68.42.36.79 1.07.79 2.16v3.26c0 .31.21.68.8.56A11.5 11.5 0 0 0 12 .7Z"
      />
    </svg>
  );
}

function App() {
  const [usuario, setUsuario] = useState(null);
  const [verificandoSessao, setVerificandoSessao] = useState(true);

  const [modoAuth, setModoAuth] = useState("login");
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [mostrarSenha, setMostrarSenha] = useState(false);

  const [aceitouTermos, setAceitouTermos] = useState(false);

  const [erroAuth, setErroAuth] = useState("");
  const [sucessoAuth, setSucessoAuth] = useState("");
  const [processandoAuth, setProcessandoAuth] = useState(false);

  const [modalLegal, setModalLegal] = useState(null);

    const [questoes, setQuestoes] = useState([]);
  const [respostas, setRespostas] = useState({});
  const [resultado, setResultado] = useState(null);

  const [buscaLivros, setBuscaLivros] = useState("");
  const [livros, setLivros] = useState([]);
  const [buscandoLivros, setBuscandoLivros] = useState(false);
  const [erroLivros, setErroLivros] = useState("");

  const [carregando, setCarregando] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState("");

  useEffect(() => {
    const parametros = new URLSearchParams(window.location.search);
    const oauthError = parametros.get("oauthError");

    if (oauthError) {
      setErroAuth(oauthError);

      window.history.replaceState(
        {},
        document.title,
        window.location.pathname
      );
    }

    verificarSessao();
  }, []);

    useEffect(() => {
    if (usuario?.tipo === "ESTUDANTE") {
      carregarQuestoes();
      return;
    }

    setQuestoes([]);
    setRespostas({});
    setResultado(null);
    setErro("");
    setCarregando(false);
  }, [usuario]);

  async function verificarSessao() {
    try {
      const response = await fetch(`${API_URL}/api/auth/me`, {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) {
        setUsuario(null);
        return;
      }

      const dados = await response.json();
      setUsuario(dados);
    } catch (error) {
      console.error("Erro ao verificar sessão:", error);
      setUsuario(null);
    } finally {
      setVerificandoSessao(false);
    }
  }

  function limparMensagensAuth() {
    setErroAuth("");
    setSucessoAuth("");
  }

  function trocarModoAuth(modo) {
    setModoAuth(modo);
    limparMensagensAuth();
    setSenha("");
    setAceitouTermos(false);

    if (modo === "login") {
      setNome("");
    }
  }

  async function fazerLogin(event) {
    event.preventDefault();
    limparMensagensAuth();

    if (!email.trim() || !senha) {
      setErroAuth("Preencha o e-mail e a senha.");
      return;
    }

    try {
      setProcessandoAuth(true);

      const response = await fetch(`${API_URL}/api/auth/login`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email: email.trim(),
          senha,
        }),
      });

      if (!response.ok) {
        setErroAuth("E-mail ou senha inválidos.");
        return;
      }

      const dados = await response.json();

      setUsuario(dados);
      setEmail("");
      setSenha("");
    } catch (error) {
      console.error("Erro no login:", error);

      setErroAuth(
        "Não foi possível conectar ao servidor. Tente novamente."
      );
    } finally {
      setProcessandoAuth(false);
    }
  }

  async function fazerCadastro(event) {
    event.preventDefault();
    limparMensagensAuth();

    if (!nome.trim() || !email.trim() || !senha) {
      setErroAuth("Preencha todos os campos.");
      return;
    }

    if (senha.length < 6) {
      setErroAuth("A senha deve possuir pelo menos 6 caracteres.");
      return;
    }

    if (!aceitouTermos) {
      setErroAuth(
        "Você precisa concordar com os Termos de Uso e a Política de Privacidade."
      );
      return;
    }

    try {
      setProcessandoAuth(true);

      const response = await fetch(`${API_URL}/api/auth/cadastro`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          nome: nome.trim(),
          email: email.trim(),
          senha,
          tipo: "ESTUDANTE",
        }),
      });

      if (!response.ok) {
        let mensagem = "Não foi possível criar a conta.";

        try {
          const dadosErro = await response.json();

          mensagem =
            dadosErro.mensagem ||
            dadosErro.message ||
            mensagem;
        } catch {
          
        }

        setErroAuth(mensagem);
        return;
      }

      setModoAuth("login");
      setNome("");
      setSenha("");
      setAceitouTermos(false);

      setSucessoAuth(
        "Conta criada com sucesso. Agora faça seu login."
      );
    } catch (error) {
      console.error("Erro no cadastro:", error);

      setErroAuth(
        "Não foi possível conectar ao servidor. Tente novamente."
      );
    } finally {
      setProcessandoAuth(false);
    }
  }

  function entrarComGoogle() {
    window.location.href =
      `${API_URL}/oauth2/authorization/google`;
  }

  function entrarComGitHub() {
    window.location.href =
      `${API_URL}/oauth2/authorization/github`;
  }

  async function fazerLogout() {
    try {
      await fetch(`${API_URL}/api/auth/logout`, {
        method: "POST",
        credentials: "include",
      });
    } catch (error) {
      console.error("Erro ao encerrar sessão:", error);
    } finally {
      setUsuario(null);
      setQuestoes([]);
      setRespostas({});
      setResultado(null);
      setErro("");
      setErroAuth("");
    }
  }

  async function carregarQuestoes() {
    try {
      setCarregando(true);
      setErro("");

      const response = await fetch(
        `${API_URL}/api/questoes/avaliacao/${AVALIACAO_ID}`,
        {
          method: "GET",
          credentials: "include",
        }
      );

      if (response.status === 401) {
        setUsuario(null);
        return;
      }

      if (!response.ok) {
        throw new Error("Erro ao buscar questões.");
      }

      const dados = await response.json();
      setQuestoes(dados);
    } catch (error) {
      console.error(error);
      setErro("Não foi possível carregar a avaliação.");
    } finally {
      setCarregando(false);
    }
    }

  async function buscarLivros(event) {
    event.preventDefault();

    const termo = buscaLivros.trim();

    if (!termo) {
      setErroLivros("Digite um assunto para buscar materiais.");
      setLivros([]);
      return;
    }

    try {
      setBuscandoLivros(true);
      setErroLivros("");

      const response = await fetch(
        `${API_URL}/api/livros?busca=${encodeURIComponent(termo)}`,
        {
          method: "GET",
          credentials: "include",
        }
      );

      if (response.status === 401) {
        setUsuario(null);
        return;
      }

      if (!response.ok) {
        throw new Error("Erro ao buscar livros.");
      }

      const dados = await response.json();

      setLivros(dados);

      if (dados.length === 0) {
        setErroLivros(
          "Nenhum material encontrado para essa busca."
        );
      }
    } catch (error) {
      console.error("Erro ao buscar livros:", error);

      setErroLivros(
        "Não foi possível buscar os materiais no momento."
      );

      setLivros([]);
    } finally {
      setBuscandoLivros(false);
    }
  }

  function selecionarResposta(questaoId, alternativa) {
    setRespostas((anteriores) => ({
      ...anteriores,
      [questaoId]: alternativa,
    }));
  }

  async function finalizarAvaliacao() {
    setErro("");

    if (questoes.length === 0) {
      setErro("Nenhuma questão disponível.");
      return;
    }

    if (Object.keys(respostas).length !== questoes.length) {
      setErro("Responda todas as questões antes de finalizar.");
      return;
    }

    const respostasFormatadas = questoes.map((questao) => ({
      questaoId: questao.id,
      resposta: respostas[questao.id],
    }));

    try {
      setEnviando(true);

      const response = await fetch(
        `${API_URL}/api/avaliacoes/finalizar`,
        {
          method: "POST",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            usuarioId: usuario.id,
            avaliacaoId: AVALIACAO_ID,
            respostas: respostasFormatadas,
          }),
        }
      );

      if (response.status === 401) {
        setUsuario(null);
        return;
      }

      if (!response.ok) {
        throw new Error("Erro ao finalizar avaliação.");
      }

      const dados = await response.json();
      setResultado(dados);

      window.scrollTo({
        top: 0,
        behavior: "smooth",
      });
    } catch (error) {
      console.error(error);
      setErro("Não foi possível finalizar a avaliação.");
    } finally {
      setEnviando(false);
    }
  }

  function renderModalLegal() {
    if (!modalLegal) {
      return null;
    }

    const termos = modalLegal === "termos";

    return (
      <div
        className="modal-overlay"
        onMouseDown={() => setModalLegal(null)}
      >
        <section
          className="modal-legal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="titulo-modal"
          onMouseDown={(event) => event.stopPropagation()}
        >
          <button
            className="modal-fechar"
            type="button"
            aria-label="Fechar"
            onClick={() => setModalLegal(null)}
          >
            ×
          </button>

          <span className="modal-etiqueta">SAP</span>

          <h2 id="titulo-modal">
            {termos
              ? "Termos de Uso"
              : "Política de Privacidade"}
          </h2>

          {termos ? (
            <div className="texto-legal">
              <p>
                Estes Termos de Uso estabelecem as condições para
                utilização do Sistema de Acompanhamento e
                Personalização da Aprendizagem (SAP).
              </p>

              <h3>1. Finalidade</h3>

              <p>
                O SAP é uma aplicação educacional destinada ao
                acompanhamento de avaliações, desempenho e
                recomendações de estudo.
              </p>

              <h3>2. Conta de acesso</h3>

              <p>
                O usuário é responsável pelas informações fornecidas
                no cadastro e pela utilização adequada de sua conta.
              </p>

              <h3>3. Uso da plataforma</h3>

              <p>
                A plataforma deve ser utilizada para finalidades
                educacionais e de acordo com as regras da instituição
                responsável por sua disponibilização.
              </p>

              <h3>4. Resultados educacionais</h3>

              <p>
                As análises e recomendações apresentadas pelo sistema
                servem como apoio ao processo de aprendizagem e devem
                ser interpretadas dentro do contexto educacional.
              </p>
            </div>
                    ) : (
            <div className="texto-legal">
              <p>
                Esta Política de Privacidade descreve como o Sistema
                de Acompanhamento e Personalização da Aprendizagem
                (SAP) coleta, utiliza e protege os dados pessoais
                necessários ao funcionamento da aplicação.
              </p>

              <h3>1. Dados pessoais coletados</h3>

              <p>
                Para criação e utilização da conta, o SAP pode tratar
                dados como nome, endereço de e-mail, tipo de usuário,
                provedor de autenticação e identificador associado ao
                provedor externo, quando aplicável.
              </p>

              <p>
                Nas contas criadas diretamente no SAP, a senha é
                armazenada de forma criptografada por meio de hash,
                não sendo mantida em texto simples.
              </p>

              <h3>2. Dados educacionais</h3>

              <p>
                Durante a utilização do sistema, podem ser tratados
                dados relacionados às avaliações, incluindo respostas,
                quantidade de acertos e erros, percentual de
                desempenho, análises e recomendações de estudo.
              </p>

              <h3>3. Finalidade do tratamento</h3>

              <p>
                Os dados são utilizados para autenticar e identificar
                usuários, controlar o acesso conforme o perfil,
                disponibilizar avaliações, registrar resultados,
                acompanhar o desempenho acadêmico, gerar recomendações
                de estudo e manter a segurança e a rastreabilidade das
                operações realizadas no sistema.
              </p>

              <h3>4. Login com Google e GitHub</h3>

              <p>
                O SAP permite autenticação por meio do Google e do
                GitHub. Nesses casos, podem ser recebidos dados
                necessários à identificação da conta, como nome,
                e-mail e identificador do provedor.
              </p>

              <p>
                O uso desses serviços também está sujeito às políticas
                de privacidade e às condições estabelecidas pelos
                respectivos provedores.
              </p>

              <h3>5. Logs de auditoria</h3>

              <p>
                Para fins de segurança e rastreabilidade, o SAP mantém
                registros de determinadas operações realizadas no
                sistema. Esses registros podem conter o identificador
                do usuário, e-mail, tipo da ação realizada, descrição
                da operação e data e hora do evento.
              </p>

              <p>
                Entre as operações que podem ser registradas estão
                cadastro, autenticação, tentativas de login sem
                sucesso, logout e finalização de avaliações.
              </p>

              <h3>6. Pesquisa de materiais externos</h3>

              <p>
                A funcionalidade "Materiais para estudo" utiliza a
                Open Library para realizar pesquisas bibliográficas.
                Para executar a consulta, o termo de pesquisa
                informado pelo usuário é enviado pelo backend do SAP
                ao serviço externo.
              </p>

              <p>
                O SAP não envia à Open Library senhas, respostas de
                avaliações, resultados acadêmicos ou logs de
                auditoria para realizar essa pesquisa.
              </p>

              <h3>7. Armazenamento e segurança</h3>

              <p>
                Os dados necessários ao funcionamento do SAP são
                armazenados no banco de dados da aplicação. O sistema
                utiliza mecanismos de autenticação, autorização por
                perfil e proteção das senhas das contas locais.
              </p>

              <p>
                Credenciais sensíveis utilizadas para configuração
                dos provedores externos não são armazenadas
                diretamente no código do frontend.
              </p>

              <h3>8. Direitos do titular</h3>

              <p>
                O titular dos dados pode solicitar informações sobre
                o tratamento de seus dados pessoais e, quando
                aplicável, requerer acesso, correção, atualização,
                anonimização, bloqueio ou eliminação de dados tratados
                em desconformidade, observadas as obrigações legais e
                acadêmicas aplicáveis ao sistema.
              </p>

              <h3>9. Transparência</h3>

              <p>
                Esta Política de Privacidade e os Termos de Uso
                permanecem disponíveis aos usuários no próprio
                sistema para consulta.
              </p>
                        </div>
          )}
        </section>
      </div>
    );
  }

  if (verificandoSessao) {
    return (
      <div className="tela-carregamento">
        <div className="logo-marca logo-carregamento">
          SAP
        </div>

        <div className="spinner" />

        <p>Preparando seu ambiente de aprendizagem...</p>
      </div>
    );
  }

  if (!usuario) {
    const cadastro = modoAuth === "cadastro";

    return (
      <div className="auth-page">
        <section className="auth-brand">
          <div className="brand-conteudo">
            <div className="logo-marca logo-claro">
              SAP
            </div>

            <span className="brand-badge">
              Aprendizagem personalizada
            </span>

            <h1>
              Aprenda melhor.
              <br />
              Evolua no seu ritmo.
            </h1>

            <p>
              Acompanhe seu desempenho, identifique pontos de melhoria
              e receba recomendações para direcionar seus estudos.
            </p>

            <div className="brand-destaques">
              <div>
                <strong>01</strong>
                <span>Avaliações direcionadas</span>
              </div>

              <div>
                <strong>02</strong>
                <span>Análise de desempenho</span>
              </div>

              <div>
                <strong>03</strong>
                <span>Recomendações de estudo</span>
              </div>
            </div>
          </div>

          <div className="brand-decoracao brand-decoracao-1" />
          <div className="brand-decoracao brand-decoracao-2" />
        </section>

        <section className="auth-area">
          <div className="auth-card">
            <div className="auth-mobile-logo">
              SAP
            </div>

            <div className="auth-cabecalho">
              <span className="auth-etiqueta">
                {cadastro
                  ? "Comece agora"
                  : "Bem-vindo de volta"}
              </span>

              <h2>
                {cadastro
                  ? "Crie sua conta"
                  : "Acesse sua conta"}
              </h2>

              <p>
                {cadastro
                  ? "Cadastre-se para iniciar sua jornada no SAP."
                  : "Entre para continuar acompanhando sua aprendizagem."}
              </p>
            </div>

            <div className="auth-tabs">
              <button
                type="button"
                className={modoAuth === "login" ? "ativo" : ""}
                onClick={() => trocarModoAuth("login")}
              >
                Entrar
              </button>

              <button
                type="button"
                className={cadastro ? "ativo" : ""}
                onClick={() => trocarModoAuth("cadastro")}
              >
                Criar conta
              </button>
            </div>

            <form
              className="auth-form"
              onSubmit={cadastro ? fazerCadastro : fazerLogin}
            >
              {cadastro && (
                <div className="campo">
                  <label htmlFor="nome">
                    Nome
                  </label>

                  <input
                    id="nome"
                    type="text"
                    autoComplete="name"
                    placeholder="Como podemos chamar você?"
                    value={nome}
                    onChange={(event) =>
                      setNome(event.target.value)
                    }
                  />
                </div>
              )}

              <div className="campo">
                <label htmlFor="email">
                  E-mail
                </label>

                <input
                  id="email"
                  type="email"
                  autoComplete="email"
                  placeholder="seuemail@exemplo.com"
                  value={email}
                  onChange={(event) =>
                    setEmail(event.target.value)
                  }
                />
              </div>

              <div className="campo">
                <label htmlFor="senha">
                  Senha
                </label>

                <div className="campo-senha">
                  <input
                    id="senha"
                    type={mostrarSenha ? "text" : "password"}
                    autoComplete={
                      cadastro
                        ? "new-password"
                        : "current-password"
                    }
                    placeholder={
                      cadastro
                        ? "Crie uma senha segura"
                        : "Digite sua senha"
                    }
                    value={senha}
                    onChange={(event) =>
                      setSenha(event.target.value)
                    }
                  />

                  <button
                    type="button"
                    className="mostrar-senha"
                    onClick={() =>
                      setMostrarSenha((anterior) => !anterior)
                    }
                  >
                    {mostrarSenha ? "Ocultar" : "Mostrar"}
                  </button>
                </div>
              </div>

              {cadastro && (
                <label className="aceite-termos">
                  <input
                    type="checkbox"
                    checked={aceitouTermos}
                    onChange={(event) =>
                      setAceitouTermos(event.target.checked)
                    }
                  />

                  <span>
                    Li e concordo com os{" "}
                    <button
                      type="button"
                      onClick={(event) => {
                        event.preventDefault();
                        setModalLegal("termos");
                      }}
                    >
                      Termos de Uso
                    </button>{" "}
                    e a{" "}
                    <button
                      type="button"
                      onClick={(event) => {
                        event.preventDefault();
                        setModalLegal("privacidade");
                      }}
                    >
                      Política de Privacidade
                    </button>
                    .
                  </span>
                </label>
              )}

              {erroAuth && (
                <div className="mensagem mensagem-erro">
                  {erroAuth}
                </div>
              )}

              {sucessoAuth && (
                <div className="mensagem mensagem-sucesso">
                  {sucessoAuth}
                </div>
              )}

              <button
                className="botao-principal"
                type="submit"
                disabled={
                  processandoAuth ||
                  (cadastro && !aceitouTermos)
                }
              >
                {processandoAuth
                  ? "Aguarde..."
                  : cadastro
                    ? "Criar minha conta"
                    : "Entrar no SAP"}
              </button>
            </form>

            <div className="separador">
              <span>ou continue com</span>
            </div>

            <div className="oauth-grid">
              <button
                className="botao-oauth"
                type="button"
                onClick={entrarComGoogle}
              >
                <GoogleIcon />
                <span>Google</span>
              </button>

              <button
                className="botao-oauth"
                type="button"
                onClick={entrarComGitHub}
              >
                <GitHubIcon />
                <span>GitHub</span>
              </button>
            </div>

            <p className="auth-alternativa">
              {cadastro
                ? "Já possui uma conta?"
                : "Ainda não possui uma conta?"}{" "}

              <button
                type="button"
                onClick={() =>
                  trocarModoAuth(cadastro ? "login" : "cadastro")
                }
              >
                {cadastro ? "Entrar" : "Cadastre-se"}
              </button>
            </p>

            {!cadastro && (
              <p className="legal-resumo">
                Consulte nossos{" "}
                <button
                  type="button"
                  onClick={() => setModalLegal("termos")}
                >
                  Termos de Uso
                </button>{" "}
                e nossa{" "}
                <button
                  type="button"
                  onClick={() => setModalLegal("privacidade")}
                >
                  Política de Privacidade
                </button>
                .
              </p>
            )}
          </div>

          <p className="auth-rodape">
            SAP · Sistema de Acompanhamento e Personalização da
            Aprendizagem
          </p>
        </section>

        {renderModalLegal()}
      </div>
    );
  }

    if (usuario?.tipo === "PROFESSOR") {
    return (
      <div className="app-page">
        <header className="topbar">
          <div className="topbar-conteudo">
            <div>
              <div className="logo-marca logo-topbar">
                SAP
              </div>

              <span className="topbar-subtitulo">
                Aprendizagem personalizada
              </span>
            </div>

            <div className="usuario-menu">
              <div className="avatar">
                {usuario.nome?.charAt(0)?.toUpperCase() || "P"}
              </div>

              <div className="usuario-info">
                <strong>{usuario.nome}</strong>
                <span>{usuario.email}</span>
              </div>

              <div className="links-legais-logado">
  <button
    type="button"
    onClick={() => setModalLegal("termos")}
  >
    Termos
  </button>

  <button
    type="button"
    onClick={() => setModalLegal("privacidade")}
  >
    Privacidade
  </button>

  <button
    className="botao-sair"
    type="button"
    onClick={fazerLogout}
  >
    Sair
  </button>
</div>
            </div>
          </div>
        </header>

        <main className="conteudo-app professor-main">
          <section className="professor-hero">
            <span className="secao-etiqueta">
              Área do professor
            </span>

            <h1>Olá, {usuario.nome}</h1>

            <p>
              Acesse as informações acadêmicas disponíveis para
              acompanhamento das atividades do SAP.
            </p>
          </section>

          <section className="professor-grid">
            <article className="professor-card">
              <div className="professor-card-icone">
                D
              </div>

              <div className="professor-card-conteudo">
                <span className="professor-card-tipo">
                  CONTEÚDO
                </span>

                <h2>Disciplinas</h2>

                <p>
                  Consulte as disciplinas cadastradas no sistema.
                </p>
              </div>
            </article>

            <article className="professor-card">
              <div className="professor-card-icone">
                A
              </div>

              <div className="professor-card-conteudo">
                <span className="professor-card-tipo">
                  CONTEÚDO
                </span>

                <h2>Assuntos</h2>

                <p>
                  Consulte os assuntos relacionados às disciplinas.
                </p>
              </div>
            </article>

            <article className="professor-card">
              <div className="professor-card-icone">
                AV
              </div>

              <div className="professor-card-conteudo">
                <span className="professor-card-tipo">
                  AVALIAÇÕES
                </span>

                <h2>Avaliações</h2>

                <p>
                  Consulte as avaliações disponíveis no sistema.
                </p>
              </div>
            </article>
          </section>

          <section className="professor-seguranca">
            <div className="professor-seguranca-icone">
              ✓
            </div>

            <div>
              <strong>
                Acesso de professor ativo
              </strong>

              <p>
                As funcionalidades de responder questões e finalizar
                avaliações são restritas aos estudantes.
              </p>
            </div>
                    </section>
        </main>

        {renderModalLegal()}
      </div>
    );
  }

  if (carregando) {
    return (
      <div className="tela-carregamento">
        <div className="logo-marca logo-carregamento">
          SAP
        </div>

        <div className="spinner" />

        <p>Carregando sua avaliação...</p>
      </div>
    );
  }

  if (resultado) {
  return (
    <div className="app-page">
      <header className="topbar">
  <div className="topbar-conteudo">
    <div>
      <div className="logo-marca logo-topbar">
        SAP
      </div>

      <span className="topbar-subtitulo">
        Aprendizagem personalizada
      </span>
    </div>

    <div className="usuario-menu">
      <div className="avatar">
        {usuario.nome?.charAt(0)?.toUpperCase() || "U"}
      </div>

      <div className="usuario-info">
        <strong>{usuario.nome}</strong>
        <span>{usuario.email}</span>
      </div>

      <div className="links-legais-logado">
        <button
          type="button"
          onClick={() => setModalLegal("termos")}
        >
          Termos
        </button>

        <button
          type="button"
          onClick={() => setModalLegal("privacidade")}
        >
          Privacidade
        </button>

        <button
          className="botao-sair"
          type="button"
          onClick={fazerLogout}
        >
          Sair
        </button>
      </div>
    </div>
  </div>
</header>
        <main className="conteudo-app">
          <section className="resultado-card">
            <span className="secao-etiqueta">
              Avaliação concluída
            </span>

            <h1>Seu resultado</h1>

            <p className="resultado-intro">
              Confira seu desempenho e use a recomendação para
              direcionar seus próximos estudos.
            </p>

            <div className="resultado-grid">
              <div className="resultado-percentual">
                <span>Desempenho</span>
                <strong>
                  {resultado.percentual}%
                </strong>
              </div>

              <div className="resultado-metrica">
                <span>Questões</span>
                <strong>
                  {resultado.totalQuestoes}
                </strong>
              </div>

              <div className="resultado-metrica">
                <span>Acertos</span>
                <strong>
                  {resultado.acertos}
                </strong>
              </div>

              <div className="resultado-metrica">
                <span>Erros</span>
                <strong>
                  {resultado.erros}
                </strong>
              </div>
            </div>

            <div className="recomendacao-card">
              <div>
                <span className="secao-etiqueta">
                  Análise personalizada
                </span>

                <h2>
                  Recomendação de estudo
                </h2>
              </div>

              <div className="analise-tags">
                <span>
                  Dificuldade:{" "}
                  <strong>
                    {resultado.dificuldade}
                  </strong>
                </span>

                <span>
                  Prioridade:{" "}
                  <strong>
                    {resultado.prioridade}
                  </strong>
                </span>
              </div>

              <p>
                {resultado.recomendacao}
              </p>
            </div>

            <button
              className="botao-principal botao-refazer"
              type="button"
              onClick={() => {
                setResultado(null);
                setRespostas({});
                setErro("");
              }}
            >
              Refazer avaliação
            </button>
                    </section>
        </main>

        {renderModalLegal()}
      </div>
    );
  }

  return (
    <div className="app-page">
      <header className="topbar">
        <div className="topbar-conteudo">
          <div>
            <div className="logo-marca logo-topbar">
              SAP
            </div>

            <span className="topbar-subtitulo">
              Aprendizagem personalizada
            </span>
          </div>

          <div className="usuario-menu">
            <div className="avatar">
              {usuario.nome?.charAt(0)?.toUpperCase() || "U"}
            </div>

            <div className="usuario-info">
              <strong>
                {usuario.nome}
              </strong>

              <span>
                {usuario.email}
              </span>
            </div>

            <div className="links-legais-logado">
  <button
    type="button"
    onClick={() => setModalLegal("termos")}
  >
    Termos
  </button>

  <button
    type="button"
    onClick={() => setModalLegal("privacidade")}
  >
    Privacidade
  </button>

  <button
    className="botao-sair"
    type="button"
    onClick={fazerLogout}
  >
    Sair
  </button>
</div>
          </div>
        </div>
      </header>

            <main className="conteudo-app">
        <section className="materiais-estudo">
          <div className="materiais-cabecalho">
            <div>
              <span className="secao-etiqueta">
                Biblioteca externa
              </span>

              <h2>Materiais para estudo</h2>

              <p>
                Pesquise livros relacionados aos assuntos que você
                está estudando. Os resultados são fornecidos pela
                Open Library.
              </p>
            </div>
          </div>

          <form
            className="materiais-busca"
            onSubmit={buscarLivros}
          >
            <input
              type="text"
              value={buscaLivros}
              onChange={(event) =>
                setBuscaLivros(event.target.value)
              }
              placeholder="Ex.: Java, SQL, banco de dados..."
              aria-label="Buscar materiais para estudo"
            />

            <button
              type="submit"
              disabled={buscandoLivros}
            >
              {buscandoLivros ? "Buscando..." : "Buscar livros"}
            </button>
          </form>

          {erroLivros && (
            <p className="materiais-erro">
              {erroLivros}
            </p>
          )}

          {livros.length > 0 && (
            <div className="materiais-grid">
              {livros.map((livro, index) => (
                <article
                  className="material-card"
                  key={livro.chave || index}
                >
                  <span className="material-tipo">
                    LIVRO
                  </span>

                  <h3>
                    {livro.titulo || "Título não informado"}
                  </h3>

                  <p className="material-autor">
                    {livro.autor || "Autor não informado"}
                  </p>

                  <div className="material-rodape">
                    <span>
                      {livro.anoPublicacao
                        ? `Primeira publicação: ${livro.anoPublicacao}`
                        : "Ano não informado"}
                    </span>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="avaliacao-cabecalho">
          <div>
            <span className="secao-etiqueta">
              Banco de Dados · SQL
            </span>

            <h1>Avaliação de SQL</h1>

            <p>
              Responda às questões abaixo. Todas as questões precisam
              ser respondidas antes da finalização.
            </p>
          </div>

          <div className="progresso-resumo">
            <strong>
              {Object.keys(respostas).length}/{questoes.length}
            </strong>

            <span>respondidas</span>
          </div>
        </section>

        <section className="lista-questoes">
          {questoes.map((questao, index) => (
            <article
              className="questao-card"
              key={questao.id}
            >
              <div className="questao-numero">
                Questão{" "}
                {String(index + 1).padStart(2, "0")}
              </div>

              <h2>
                {questao.enunciado}
              </h2>

              <div className="alternativas">
                {[
                  ["A", questao.alternativaA],
                  ["B", questao.alternativaB],
                  ["C", questao.alternativaC],
                  ["D", questao.alternativaD],
                ].map(([letra, texto]) => {
                  const selecionada =
                    respostas[questao.id] === letra;

                  return (
                    <label
                      className={`alternativa ${
                        selecionada ? "selecionada" : ""
                      }`}
                      key={letra}
                    >
                      <input
                        type="radio"
                        name={`questao-${questao.id}`}
                        value={letra}
                        checked={selecionada}
                        onChange={() =>
                          selecionarResposta(
                            questao.id,
                            letra
                          )
                        }
                      />

                      <span className="alternativa-letra">
                        {letra}
                      </span>

                      <span>
                        {texto}
                      </span>
                    </label>
                  );
                })}
              </div>
            </article>
          ))}
        </section>

        {erro && (
          <div className="mensagem mensagem-erro erro-avaliacao">
            {erro}
          </div>
        )}

        <div className="avaliacao-acoes">
          <div>
            <strong>
              {Object.keys(respostas).length} de {questoes.length}
            </strong>{" "}
            questões respondidas
          </div>

          <button
            className="botao-principal"
            type="button"
            onClick={finalizarAvaliacao}
            disabled={
              enviando ||
              questoes.length === 0
            }
          >
            {enviando
              ? "Finalizando..."
              : "Finalizar avaliação"}
          </button>
        </div>
      </main>

      {renderModalLegal()}
    </div>
  );
}

export default App;