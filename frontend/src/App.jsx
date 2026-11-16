import { useEffect, useState } from "react";
import "./App.css";

const API_URL = import.meta.env.VITE_API_URL;

async function fetchProtegido(url, options = {}) {
  const respostaCsrf = await fetch(`${API_URL}/api/auth/csrf`, {
    method: "GET",
    credentials: "include",
  });

  if (!respostaCsrf.ok) {
    throw new Error("Não foi possível obter o token CSRF.");
  }

  const csrf = await respostaCsrf.json();

  return fetch(url, {
    ...options,
    credentials: "include",
    headers: {
      ...options.headers,
      [csrf.headerName]: csrf.token,
    },
  });
}

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
  const [assuntosPorDisciplina, setAssuntosPorDisciplina] = useState({});
  const [disciplinaAssuntosAberta, setDisciplinaAssuntosAberta] = useState(null);
  const [carregandoAssuntos, setCarregandoAssuntos] = useState(null);
  const [usuario, setUsuario] = useState(null);
  const [paginaProfessor, setPaginaProfessor] = useState("painel");
  const [avaliacoesProfessor, setAvaliacoesProfessor] = useState([]);
  const [formularioAvaliacaoAberto, setFormularioAvaliacaoAberto] = useState(false);
  const [tituloNovaAvaliacao, setTituloNovaAvaliacao] = useState("");
  const [disciplinaNovaAvaliacao, setDisciplinaNovaAvaliacao] = useState("");
  const [assuntoNovaAvaliacao, setAssuntoNovaAvaliacao] = useState("");
  const [erroCadastroAvaliacao, setErroCadastroAvaliacao] = useState("");
  const [sucessoCadastroAvaliacao, setSucessoCadastroAvaliacao] = useState("");
  const [salvandoAvaliacao, setSalvandoAvaliacao] = useState(false);
  const [avaliacaoEmEdicao, setAvaliacaoEmEdicao] = useState(null);
  const [tituloEdicaoAvaliacao, setTituloEdicaoAvaliacao] = useState("");
  const [disciplinaEdicaoAvaliacao, setDisciplinaEdicaoAvaliacao] = useState("");
  const [assuntoEdicaoAvaliacao, setAssuntoEdicaoAvaliacao] = useState("");
  const [erroEdicaoAvaliacao, setErroEdicaoAvaliacao] = useState("");
  const [salvandoEdicaoAvaliacao, setSalvandoEdicaoAvaliacao] = useState(false);
  const [avaliacaoParaExcluir, setAvaliacaoParaExcluir] = useState(null);
  const [excluindoAvaliacao, setExcluindoAvaliacao] = useState(false);
  const [erroExclusaoAvaliacao, setErroExclusaoAvaliacao] = useState("");
  const [avaliacaoGerenciandoQuestoes, setAvaliacaoGerenciandoQuestoes] = useState(null);
  const [questoesProfessor, setQuestoesProfessor] = useState([]);
  const [carregandoQuestoesProfessor, setCarregandoQuestoesProfessor] = useState(false);
  const [erroQuestoesProfessor, setErroQuestoesProfessor] = useState("");
  const [formularioQuestaoAberto, setFormularioQuestaoAberto] = useState(false);
  const [enunciadoNovaQuestao, setEnunciadoNovaQuestao] = useState("");
  const [alternativaANovaQuestao, setAlternativaANovaQuestao] = useState("");
  const [alternativaBNovaQuestao, setAlternativaBNovaQuestao] = useState("");
  const [alternativaCNovaQuestao, setAlternativaCNovaQuestao] = useState("");
  const [alternativaDNovaQuestao, setAlternativaDNovaQuestao] = useState("");
  const [respostaCorretaNovaQuestao, setRespostaCorretaNovaQuestao] = useState("");
  const [salvandoNovaQuestao, setSalvandoNovaQuestao] = useState(false);
  const [erroCadastroQuestao, setErroCadastroQuestao] = useState("");
  const [sucessoCadastroQuestao, setSucessoCadastroQuestao] = useState("");
  const [questaoEmEdicao, setQuestaoEmEdicao] = useState(null);
  const [salvandoEdicaoQuestao, setSalvandoEdicaoQuestao] = useState(false);
  const [erroEdicaoQuestao, setErroEdicaoQuestao] = useState("");
  const [sucessoEdicaoQuestao, setSucessoEdicaoQuestao] = useState("");
  const [questaoParaExcluir, setQuestaoParaExcluir] = useState(null);
  const [excluindoQuestao, setExcluindoQuestao] = useState(false);
  const [erroExclusaoQuestao, setErroExclusaoQuestao] = useState("");
  const [sucessoExclusaoQuestao, setSucessoExclusaoQuestao] = useState("");
  const [carregandoAvaliacoesProfessor, setCarregandoAvaliacoesProfessor] = useState(false);
  const [erroAvaliacoesProfessor, setErroAvaliacoesProfessor] = useState("");
  const [buscaAvaliacoesProfessor, setBuscaAvaliacoesProfessor] = useState("");
  const [disciplinasProfessor, setDisciplinasProfessor] = useState([]);
  const [assuntosProfessor, setAssuntosProfessor] = useState([]);
  const [carregandoConteudoProfessor, setCarregandoConteudoProfessor] = useState(false);
  const [erroConteudoProfessor, setErroConteudoProfessor] = useState("");
  const [disciplinaExpandidaProfessor, setDisciplinaExpandidaProfessor] = useState(null);
  const [buscaConteudoProfessor, setBuscaConteudoProfessor] = useState("");
  const [formularioDisciplinaAberto, setFormularioDisciplinaAberto] = useState(false);
  const [nomeNovaDisciplina, setNomeNovaDisciplina] = useState("");
  const [descricaoNovaDisciplina, setDescricaoNovaDisciplina] = useState("");
  const [salvandoDisciplina, setSalvandoDisciplina] = useState(false);
  const [erroCadastroDisciplina, setErroCadastroDisciplina] = useState("");
  const [sucessoCadastroDisciplina, setSucessoCadastroDisciplina] = useState("");
  const [disciplinaParaExcluir, setDisciplinaParaExcluir] = useState(null);
  const [excluindoDisciplina, setExcluindoDisciplina] = useState(false);
  const [erroExclusaoDisciplina, setErroExclusaoDisciplina] = useState("");
  const [sucessoExclusaoDisciplina, setSucessoExclusaoDisciplina] = useState("");
  const [formularioAssuntoAberto, setFormularioAssuntoAberto] = useState(false);
  const [disciplinaNovoAssunto, setDisciplinaNovoAssunto] = useState("");
  const [nomeNovoAssunto, setNomeNovoAssunto] = useState("");
  const [descricaoNovoAssunto, setDescricaoNovoAssunto] = useState("");
  const [salvandoAssunto, setSalvandoAssunto] = useState(false);
  const [erroCadastroAssunto, setErroCadastroAssunto] = useState("");
  const [sucessoCadastroAssunto, setSucessoCadastroAssunto] = useState("");
  const [assuntoParaExcluir, setAssuntoParaExcluir] = useState(null);
  const [excluindoAssunto, setExcluindoAssunto] = useState(false);
  const [erroExclusaoAssunto, setErroExclusaoAssunto] = useState("");
  const [sucessoExclusaoAssunto, setSucessoExclusaoAssunto] = useState("");
  const [cursosProfessor, setCursosProfessor] = useState([]);
  const [semestresProfessor, setSemestresProfessor] = useState([]);
  const [carregandoCursosProfessor, setCarregandoCursosProfessor] = useState(false);
  const [erroCursosProfessor, setErroCursosProfessor] = useState("");
  const [buscaCursosProfessor, setBuscaCursosProfessor] = useState("");
  const [cursoExpandidoProfessor, setCursoExpandidoProfessor] = useState(null);
  const [cursoParaExcluir, setCursoParaExcluir] = useState(null);
  const [erroExclusaoCurso, setErroExclusaoCurso] = useState("");
  const [sucessoExclusaoCurso, setSucessoExclusaoCurso] = useState("");
  const [excluindoCurso, setExcluindoCurso] = useState(false);

  const [formularioCursoAberto, setFormularioCursoAberto] = useState(false);
  const [nomeNovoCurso, setNomeNovoCurso] = useState("");
  const [descricaoNovoCurso, setDescricaoNovoCurso] = useState("");
  const [salvandoCurso, setSalvandoCurso] = useState(false);
  const [erroCadastroCurso, setErroCadastroCurso] = useState("");
  const [sucessoCadastroCurso, setSucessoCadastroCurso] = useState("");

  const [formularioSemestreAberto, setFormularioSemestreAberto] = useState(false);
  const [cursoNovoSemestre, setCursoNovoSemestre] = useState("");
  const [numeroNovoSemestre, setNumeroNovoSemestre] = useState("");
  const [salvandoSemestre, setSalvandoSemestre] = useState(false);
  const [erroCadastroSemestre, setErroCadastroSemestre] = useState("");
  const [sucessoCadastroSemestre, setSucessoCadastroSemestre] = useState("");

  const [semestreParaExcluir, setSemestreParaExcluir] = useState(null);
  const [excluindoSemestre, setExcluindoSemestre] = useState(false);
  const [erroExclusaoSemestre, setErroExclusaoSemestre] = useState("");
  const [sucessoExclusaoSemestre, setSucessoExclusaoSemestre] = useState("");

async function excluirSemestreProfessor() {
  if (!semestreParaExcluir || excluindoSemestre) {
    return;
  }

  setExcluindoSemestre(true);
  setErroExclusaoSemestre("");
  setSucessoExclusaoSemestre("");

  try {
    const resposta = await fetchProtegido(
      `/api/semestres-cursos/${semestreParaExcluir.id}`,
      {
        method: "DELETE",
        credentials: "include",
      }
    );

    if (!resposta.ok) {
      const dados = await resposta.json().catch(() => ({}));

      throw new Error(
        dados.erro || "Não foi possível excluir o semestre."
      );
    }

    setSemestresProfessor((anteriores) =>
      anteriores.filter(
        (semestre) => semestre.id !== semestreParaExcluir.id
      )
    );

    setSucessoExclusaoSemestre("Semestre excluído com sucesso!");
    setSemestreParaExcluir(null);
  } catch (erro) {
    setErroExclusaoSemestre(
      erro.message || "Erro ao excluir semestre."
    );
  } finally {
    setExcluindoSemestre(false);
  }
}

async function cadastrarSemestreProfessor(evento) {
  evento.preventDefault();

  const cursoId = Number(cursoNovoSemestre);
  const numero = Number(numeroNovoSemestre);

  setErroCadastroSemestre("");
  setSucessoCadastroSemestre("");

  if (!cursoId || !Number.isInteger(numero) || numero < 1 || numero > 20) {
    setErroCadastroSemestre("Selecione um curso e informe um semestre válido entre 1 e 20.");
    return;
  }

  setSalvandoSemestre(true);

  try {
    const resposta = await fetchProtegido("/api/semestres-cursos", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        cursoId,
        numero,
      }),
    });

    const dados = await resposta.json();

    if (!resposta.ok) {
      throw new Error(
        dados.erro || "Não foi possível cadastrar o semestre."
      );
    }

    setSemestresProfessor((anteriores) =>
      [...anteriores, dados].sort((a, b) => a.numero - b.numero)
    );

    setCursoExpandidoProfessor(cursoId);
    setCursoNovoSemestre("");
    setNumeroNovoSemestre("");
    setFormularioSemestreAberto(false);
    setSucessoCadastroSemestre("Semestre cadastrado com sucesso!");
  } catch (erro) {
    setErroCadastroSemestre(
      erro.message || "Erro ao cadastrar semestre."
    );
  } finally {
    setSalvandoSemestre(false);
  }
}


async function excluirCursoProfessor() {
  if (!cursoParaExcluir || excluindoCurso) {
    return;
  }

  setExcluindoCurso(true);
  setErroExclusaoCurso("");
  setSucessoExclusaoCurso("");

  try {
    const resposta = await fetchProtegido(
      `/api/cursos/${cursoParaExcluir.id}`,
      {
        method: "DELETE",
        credentials: "include",
      }
    );

    if (!resposta.ok) {
      const dados = await resposta.json().catch(() => ({}));

      throw new Error(
        dados.erro || "Não foi possível excluir o curso."
      );
    }

    setCursosProfessor((anteriores) =>
      anteriores.filter(
        (curso) => curso.id !== cursoParaExcluir.id
      )
    );

    setSemestresProfessor((anteriores) =>
      anteriores.filter(
        (semestre) => semestre.cursoId !== cursoParaExcluir.id
      )
    );

    setCursoExpandidoProfessor(null);
    setSucessoExclusaoCurso("Curso excluído com sucesso!");
    setCursoParaExcluir(null);
  } catch (erro) {
    setErroExclusaoCurso(
      erro.message || "Erro ao excluir curso."
    );
  } finally {
    setExcluindoCurso(false);
  }
}

useEffect(() => {
  if (paginaProfessor !== "avaliacoes" || usuario?.tipo !== "PROFESSOR") {
    return;
  }

  carregarAvaliacoesProfessor();
}, [paginaProfessor, usuario?.id, usuario?.tipo]);

function abrirExclusaoAvaliacao(avaliacao) {
  setAvaliacaoParaExcluir(avaliacao);
  setErroExclusaoAvaliacao("");
  setAvaliacaoEmEdicao(null);
  setFormularioAvaliacaoAberto(false);
  setSucessoCadastroAvaliacao("");
}

async function excluirAvaliacaoProfessor() {
  if (!avaliacaoParaExcluir || excluindoAvaliacao) {
    return;
  }

  setErroExclusaoAvaliacao("");
  setExcluindoAvaliacao(true);

  try {
    const resposta = await fetchProtegido(
      `/api/avaliacoes/professor/${avaliacaoParaExcluir.id}`,
      {
        method: "DELETE",
        credentials: "include"
      }
    );

    const dados = await resposta.json().catch(() => ({}));

    if (!resposta.ok) {
      throw new Error(
        dados.erro || "Não foi possível excluir a avaliação."
      );
    }

    setAvaliacoesProfessor((avaliacoesAtuais) =>
      avaliacoesAtuais.filter(
        (avaliacao) => avaliacao.id !== avaliacaoParaExcluir.id
      )
    );

    setAvaliacaoParaExcluir(null);
    setErroExclusaoAvaliacao("");
    setSucessoCadastroAvaliacao("Avaliação excluída com sucesso!");
  } catch (erro) {
    setErroExclusaoAvaliacao(
      erro.message || "Erro ao excluir avaliação."
    );
  } finally {
    setExcluindoAvaliacao(false);
  }
}

async function carregarQuestoesProfessor(avaliacao) {
  if (!avaliacao?.id) {
    return;
  }

  setAvaliacaoGerenciandoQuestoes(avaliacao);
  setQuestoesProfessor([]);
  setErroQuestoesProfessor("");
  setCarregandoQuestoesProfessor(true);

  setAvaliacaoEmEdicao(null);
  setAvaliacaoParaExcluir(null);
  setFormularioAvaliacaoAberto(false);

  try {
    const resposta = await fetchProtegido(
      `/api/questoes/professor/avaliacao/${avaliacao.id}`,
      {
        method: "GET",
        credentials: "include"
      }
    );

    const dados = await resposta.json().catch(() => ({}));

    if (!resposta.ok) {
      throw new Error(
        dados.erro || "Não foi possível carregar as questões."
      );
    }

    if (!Array.isArray(dados)) {
      throw new Error("Formato inválido das questões recebidas.");
    }

    setQuestoesProfessor(dados);
  } catch (erro) {
    setErroQuestoesProfessor(
      erro.message || "Erro ao carregar as questões."
    );
  } finally {
    setCarregandoQuestoesProfessor(false);
  }
}

async function salvarNovaQuestao(evento) {
  evento.preventDefault();

  if (!avaliacaoGerenciandoQuestoes?.id) {
    setErroCadastroQuestao("Nenhuma avaliação foi selecionada.");
    return;
  }

  const enunciado = enunciadoNovaQuestao.trim();
  const alternativaA = alternativaANovaQuestao.trim();
  const alternativaB = alternativaBNovaQuestao.trim();
  const alternativaC = alternativaCNovaQuestao.trim();
  const alternativaD = alternativaDNovaQuestao.trim();

  if (
    !enunciado ||
    !alternativaA ||
    !alternativaB ||
    !alternativaC ||
    !alternativaD ||
    !respostaCorretaNovaQuestao
  ) {
    setErroCadastroQuestao("Preencha todos os campos da questão.");
    return;
  }

  setSalvandoNovaQuestao(true);
  setErroCadastroQuestao("");
  setSucessoCadastroQuestao("");

  try {
    const resposta = await fetchProtegido(
      "/api/questoes/professor",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          avaliacaoId: avaliacaoGerenciandoQuestoes.id,
          enunciado,
          alternativaA,
          alternativaB,
          alternativaC,
          alternativaD,
          respostaCorreta: respostaCorretaNovaQuestao
        })
      }
    );

    const dados = await resposta.json();

    if (!resposta.ok) {
      throw new Error(dados.erro || "Não foi possível cadastrar a questão.");
    }

    setFormularioQuestaoAberto(false);
    setEnunciadoNovaQuestao("");
    setAlternativaANovaQuestao("");
    setAlternativaBNovaQuestao("");
    setAlternativaCNovaQuestao("");
    setAlternativaDNovaQuestao("");
    setRespostaCorretaNovaQuestao("");
    setSucessoCadastroQuestao("Questão cadastrada com sucesso!");

    await carregarQuestoesProfessor(avaliacaoGerenciandoQuestoes);
  } catch (erro) {
    setErroCadastroQuestao(erro.message || "Erro ao cadastrar questão.");
  } finally {
    setSalvandoNovaQuestao(false);
  }
}

async function salvarEdicaoQuestao(evento) {
  evento.preventDefault();

  if (!questaoEmEdicao?.id) {
    setErroEdicaoQuestao("Nenhuma questão foi selecionada.");
    return;
  }

  const enunciado = (questaoEmEdicao.enunciado ?? "").trim();
  const alternativaA = (questaoEmEdicao.alternativaA ?? "").trim();
  const alternativaB = (questaoEmEdicao.alternativaB ?? "").trim();
  const alternativaC = (questaoEmEdicao.alternativaC ?? "").trim();
  const alternativaD = (questaoEmEdicao.alternativaD ?? "").trim();
  const respostaCorreta = (questaoEmEdicao.respostaCorreta ?? "").trim().toUpperCase();

  if (
    !enunciado ||
    !alternativaA ||
    !alternativaB ||
    !alternativaC ||
    !alternativaD ||
    !respostaCorreta
  ) {
    setErroEdicaoQuestao("Preencha todos os campos da questão.");
    return;
  }

  if (
    [alternativaA, alternativaB, alternativaC, alternativaD].some(
      (alternativa) => alternativa.length > 255
    )
  ) {
    setErroEdicaoQuestao("Cada alternativa deve ter no máximo 255 caracteres.");
    return;
  }

  if (!["A", "B", "C", "D"].includes(respostaCorreta)) {
    setErroEdicaoQuestao("Selecione uma resposta correta válida.");
    return;
  }

  setSalvandoEdicaoQuestao(true);
  setErroEdicaoQuestao("");
  setSucessoEdicaoQuestao("");

  try {
    const resposta = await fetchProtegido(
      `/api/questoes/professor/${questaoEmEdicao.id}`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          enunciado,
          alternativaA,
          alternativaB,
          alternativaC,
          alternativaD,
          respostaCorreta
        })
      }
    );

    const dados = await resposta.json();

    if (!resposta.ok) {
      throw new Error(dados.erro || "Não foi possível atualizar a questão.");
    }

    setQuestaoEmEdicao(null);
    setSucessoEdicaoQuestao("Questão atualizada com sucesso!");

    if (avaliacaoGerenciandoQuestoes) {
      await carregarQuestoesProfessor(avaliacaoGerenciandoQuestoes);
    }
  } catch (erro) {
    setErroEdicaoQuestao(erro.message || "Erro ao editar a questão.");
  } finally {
    setSalvandoEdicaoQuestao(false);
  }
}

async function excluirQuestaoProfessor() {
  if (!questaoParaExcluir || !avaliacaoGerenciandoQuestoes || excluindoQuestao) {
    return;
  }

  setExcluindoQuestao(true);
  setErroExclusaoQuestao("");
  setSucessoExclusaoQuestao("");

  try {
    const resposta = await fetchProtegido(
      `/api/questoes/professor/${questaoParaExcluir.id}`,
      {
        method: "DELETE"
      }
    );

    const dados = await resposta.json();

    if (!resposta.ok) {
      throw new Error(dados.erro || "Não foi possível excluir a questão.");
    }

    setQuestaoParaExcluir(null);
    setSucessoExclusaoQuestao("Questão excluída com sucesso!");

    await carregarQuestoesProfessor(avaliacaoGerenciandoQuestoes);
  } catch (erro) {
    setErroExclusaoQuestao(
      erro.message || "Ocorreu um erro ao excluir a questão."
    );
  } finally {
    setExcluindoQuestao(false);
  }
}

function abrirEdicaoAvaliacao(avaliacao) {
  setAvaliacaoEmEdicao(avaliacao);
  setTituloEdicaoAvaliacao(avaliacao.titulo || "");
  setDisciplinaEdicaoAvaliacao(String(avaliacao.disciplinaId || ""));
  setAssuntoEdicaoAvaliacao(String(avaliacao.assuntoId || ""));
  setErroEdicaoAvaliacao("");
  setSucessoCadastroAvaliacao("");
  setFormularioAvaliacaoAberto(false);
}


async function salvarEdicaoAvaliacao(evento) {
  evento.preventDefault();

  if (!avaliacaoEmEdicao) {
    return;
  }

  setErroEdicaoAvaliacao("");

  const titulo = tituloEdicaoAvaliacao.trim();

  if (!titulo || !disciplinaEdicaoAvaliacao || !assuntoEdicaoAvaliacao) {
    setErroEdicaoAvaliacao("Preencha todos os campos da avaliação.");
    return;
  }

  setSalvandoEdicaoAvaliacao(true);

  try {
    const resposta = await fetchProtegido(
      `/api/avaliacoes/professor/${avaliacaoEmEdicao.id}`,
      {
        method: "PUT",
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          titulo,
          assuntoId: Number(assuntoEdicaoAvaliacao)
        })
      }
    );

    const dados = await resposta.json().catch(() => ({}));

    if (!resposta.ok) {
      throw new Error(dados.erro || "Não foi possível atualizar a avaliação.");
    }

    setAvaliacoesProfessor((avaliacoesAtuais) =>
      avaliacoesAtuais.map((avaliacao) =>
        avaliacao.id === dados.id ? dados : avaliacao
      )
    );

    setAvaliacaoEmEdicao(null);
    setTituloEdicaoAvaliacao("");
    setDisciplinaEdicaoAvaliacao("");
    setAssuntoEdicaoAvaliacao("");
    setSucessoCadastroAvaliacao("Avaliação atualizada com sucesso!");
  } catch (erro) {
    setErroEdicaoAvaliacao(
      erro.message || "Erro ao atualizar a avaliação."
    );
  } finally {
    setSalvandoEdicaoAvaliacao(false);
  }
}

async function cadastrarAvaliacaoProfessor(evento) {
  evento.preventDefault();

  setErroCadastroAvaliacao("");
  setSucessoCadastroAvaliacao("");

  const titulo = tituloNovaAvaliacao.trim();

  if (!titulo || !disciplinaNovaAvaliacao || !assuntoNovaAvaliacao) {
    setErroCadastroAvaliacao("Preencha todos os campos da avaliação.");
    return;
  }

  setSalvandoAvaliacao(true);

  try {
    const resposta = await fetchProtegido("/api/avaliacoes/professor", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        titulo,
        assuntoId: Number(assuntoNovaAvaliacao)
      })
    });

    const dados = await resposta.json().catch(() => ({}));

    if (!resposta.ok) {
      throw new Error(dados.erro || "Não foi possível cadastrar a avaliação.");
    }

    setAvaliacoesProfessor((avaliacoesAtuais) => [
      ...avaliacoesAtuais,
      dados
    ]);

    setTituloNovaAvaliacao("");
    setDisciplinaNovaAvaliacao("");
    setAssuntoNovaAvaliacao("");
    setFormularioAvaliacaoAberto(false);
    setSucessoCadastroAvaliacao("Avaliação cadastrada com sucesso!");
  } catch (erro) {
    setErroCadastroAvaliacao(
      erro.message || "Erro ao cadastrar avaliação."
    );
  } finally {
    setSalvandoAvaliacao(false);
  }
}

async function carregarAvaliacoesProfessor() {
  setCarregandoAvaliacoesProfessor(true);
  setErroAvaliacoesProfessor("");

  try {
    const resposta = await fetchProtegido(
      "/api/avaliacoes/professor",
      {
        method: "GET",
        credentials: "include",
      }
    );

    if (!resposta.ok) {
      const dados = await resposta.json().catch(() => ({}));

      throw new Error(
        dados.erro || "Não foi possível carregar as avaliações."
      );
    }

    const dados = await resposta.json();

    setAvaliacoesProfessor(dados);
  } catch (erro) {
    setAvaliacoesProfessor([]);
    setErroAvaliacoesProfessor(
      erro.message || "Erro ao carregar avaliações."
    );
  } finally {
    setCarregandoAvaliacoesProfessor(false);
  }
}

async function cadastrarCursoProfessor(evento) {
  evento.preventDefault();

  const nome = nomeNovoCurso.trim();
  const descricao = descricaoNovoCurso.trim();

  setErroCadastroCurso("");
  setSucessoCadastroCurso("");

  if (!nome) {
    setErroCadastroCurso("Informe o nome do curso.");
    return;
  }

  setSalvandoCurso(true);

  try {
    const resposta = await fetchProtegido("/api/cursos", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        nome,
        descricao,
      }),
    });

    const dados = await resposta.json();

    if (!resposta.ok) {
      throw new Error(
        dados.erro || "Não foi possível cadastrar o curso."
      );
    }

    setCursosProfessor((anteriores) =>
      [...anteriores, dados].sort((a, b) =>
        a.nome.localeCompare(b.nome, "pt-BR")
      )
    );

    setNomeNovoCurso("");
    setDescricaoNovoCurso("");
    setSucessoCadastroCurso("Curso cadastrado com sucesso!");
    setFormularioCursoAberto(false);
  } catch (erro) {
    setErroCadastroCurso(
      erro.message || "Erro ao cadastrar curso."
    );
  } finally {
    setSalvandoCurso(false);
  }
}

  async function cadastrarDisciplinaProfessor(e) {
  e.preventDefault();

  const nome = nomeNovaDisciplina.trim();
  const descricao = descricaoNovaDisciplina.trim();

  if (!nome) {
    setErroCadastroDisciplina("Informe o nome da disciplina.");
    setSucessoCadastroDisciplina("");
    return;
  }

  setSalvandoDisciplina(true);
  setErroCadastroDisciplina("");
  setSucessoCadastroDisciplina("");

  try {
    const resposta = await fetchProtegido("/api/disciplinas", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        nome,
        descricao
      })
    });

    if (!resposta.ok) {
      if (resposta.status === 403) {
        throw new Error("Você não tem permissão para cadastrar disciplinas.");
      }

      throw new Error("Não foi possível cadastrar a disciplina.");
    }

    const novaDisciplina = await resposta.json();

    setDisciplinasProfessor((disciplinasAtuais) => [
      ...disciplinasAtuais,
      novaDisciplina
    ].sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR")));

    setNomeNovaDisciplina("");
    setDescricaoNovaDisciplina("");
    setFormularioDisciplinaAberto(false);
    setSucessoCadastroDisciplina("Disciplina cadastrada com sucesso!");
  } catch (erro) {
    setErroCadastroDisciplina(erro.message);
  } finally {
    setSalvandoDisciplina(false);
  }
}

async function cadastrarAssuntoProfessor(e) {
  e.preventDefault();

  setErroCadastroAssunto("");
  setSucessoCadastroAssunto("");

  if (!disciplinaNovoAssunto) {
    setErroCadastroAssunto("Selecione uma disciplina.");
    return;
  }

  if (!nomeNovoAssunto.trim()) {
    setErroCadastroAssunto("Informe o nome do assunto.");
    return;
  }

  setSalvandoAssunto(true);

  try {
    const resposta = await fetchProtegido("/api/assuntos", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        nome: nomeNovoAssunto.trim(),
        descricao: descricaoNovoAssunto.trim(),
        disciplina: {
          id: Number(disciplinaNovoAssunto),
        },
      }),
    });

    const dados = await resposta.json();

    if (!resposta.ok) {
      throw new Error(dados.erro || "Não foi possível cadastrar o assunto.");
    }

    setAssuntosProfessor((anteriores) => [...anteriores, dados]);

    setDisciplinaExpandidaProfessor(Number(disciplinaNovoAssunto));
    setFormularioAssuntoAberto(false);
    setDisciplinaNovoAssunto("");
    setNomeNovoAssunto("");
    setDescricaoNovoAssunto("");

    setSucessoCadastroAssunto("Assunto cadastrado com sucesso!");
  } catch (erro) {
    setErroCadastroAssunto(erro.message);
  } finally {
    setSalvandoAssunto(false);
  }
}


async function excluirAssuntoProfessor() {
  if (!assuntoParaExcluir || excluindoAssunto) {
    return;
  }

  setExcluindoAssunto(true);
  setErroExclusaoAssunto("");
  setSucessoExclusaoAssunto("");

  try {
    const resposta = await fetchProtegido(
      `/api/assuntos/${assuntoParaExcluir.id}`,
      {
        method: "DELETE",
        credentials: "include",
      }
    );

    if (!resposta.ok) {
      const dados = await resposta.json().catch(() => ({}));

      throw new Error(
        dados.erro || "Não foi possível excluir o assunto."
      );
    }

    setAssuntosProfessor((anteriores) =>
      anteriores.filter(
        (assunto) => assunto.id !== assuntoParaExcluir.id
      )
    );

    setAssuntoParaExcluir(null);
    setSucessoExclusaoAssunto("Assunto excluído com sucesso!");
  } catch (erro) {
    setErroExclusaoAssunto(
      erro.message || "Não foi possível excluir o assunto."
    );
  } finally {
    setExcluindoAssunto(false);
  }
}

async function excluirDisciplinaProfessor() {
  if (!disciplinaParaExcluir || excluindoDisciplina) {
    return;
  }

  const disciplina = disciplinaParaExcluir;

  setExcluindoDisciplina(true);
  setErroExclusaoDisciplina("");
  setSucessoExclusaoDisciplina("");

  try {
    const resposta = await fetchProtegido(
      `/api/disciplinas/${disciplina.id}`,
      {
        method: "DELETE",
        credentials: "include"
      }
    );

    if (!resposta.ok) {
      if (resposta.status === 403) {
        throw new Error("Você não tem permissão para excluir disciplinas.");
      }

      const dados = await resposta.json().catch(() => null);

      throw new Error(
        dados?.erro || "Não foi possível excluir a disciplina."
      );
    }

    setDisciplinasProfessor((disciplinasAtuais) =>
      disciplinasAtuais.filter((item) => item.id !== disciplina.id)
    );

    setDisciplinaExpandidaProfessor((idAtual) =>
      idAtual === disciplina.id ? null : idAtual
    );

    setDisciplinaParaExcluir(null);
    setSucessoExclusaoDisciplina(
      `Disciplina "${disciplina.nome}" excluída com sucesso!`
    );
  } catch (erro) {
    setErroExclusaoDisciplina(erro.message);
  } finally {
    setExcluindoDisciplina(false);
  }
}

  const [abaAdmin, setAbaAdmin] = useState("acessos");
  const [usuariosAdmin, setUsuariosAdmin] = useState([]);
  const [carregandoUsuariosAdmin, setCarregandoUsuariosAdmin] = useState(false);
  const [erroUsuariosAdmin, setErroUsuariosAdmin] = useState("");
  const [buscaUsuariosAdmin, setBuscaUsuariosAdmin] = useState("");
  const [alterandoAcessoAdmin, setAlterandoAcessoAdmin] = useState(null);
  const [nomeProfessorAdmin, setNomeProfessorAdmin] = useState("");
  const [emailProfessorAdmin, setEmailProfessorAdmin] = useState("");
  const [senhaProfessorAdmin, setSenhaProfessorAdmin] = useState("");
  const [confirmarSenhaProfessorAdmin, setConfirmarSenhaProfessorAdmin] = useState("");
  const [cadastrandoProfessorAdmin, setCadastrandoProfessorAdmin] = useState(false);
  const [erroCadastroProfessorAdmin, setErroCadastroProfessorAdmin] = useState("");
  const [sucessoCadastroProfessorAdmin, setSucessoCadastroProfessorAdmin] = useState("");
  const [erroAlteracaoAcessoAdmin, setErroAlteracaoAcessoAdmin] = useState("");
  const [resumoAdmin, setResumoAdmin] = useState({
  totalAlunos: 0,
  totalProfessores: 0
});

  const [carregandoResumoAdmin, setCarregandoResumoAdmin] = useState(false);
  const [erroResumoAdmin, setErroResumoAdmin] = useState("");
  const [verificandoSessao, setVerificandoSessao] = useState(true);
  const [configuracaoAcademica, setConfiguracaoAcademica] = useState(null);

  const [verificandoConfiguracao, setVerificandoConfiguracao] = useState(false);
  const [cursos, setCursos] = useState([]);
  const [cursoSelecionado, setCursoSelecionado] = useState("");
  const [semestreSelecionado, setSemestreSelecionado] = useState("");
  const [ofertasDisciplinas, setOfertasDisciplinas] = useState([]);
  const [disciplinasMatriculadas, setDisciplinasMatriculadas] = useState([]);
  const [disciplinasSelecionadas, setDisciplinasSelecionadas] = useState([]);
  const [salvandoConfiguracao, setSalvandoConfiguracao] = useState(false);
  const [erroConfiguracao, setErroConfiguracao] = useState("");

  const [modoAuth, setModoAuth] = useState("login");
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [mostrarSenha, setMostrarSenha] = useState(false);

  const [paginaRecuperacao, setPaginaRecuperacao] = useState(
    () => window.location.pathname
  );
  const [emailRecuperacao, setEmailRecuperacao] = useState("");
  const [novaSenhaRecuperacao, setNovaSenhaRecuperacao] = useState("");
  const [confirmarSenhaRecuperacao, setConfirmarSenhaRecuperacao] = useState("");
  const [carregandoRecuperacao, setCarregandoRecuperacao] = useState(false);
  const [erroRecuperacao, setErroRecuperacao] = useState("");
  const [sucessoRecuperacao, setSucessoRecuperacao] = useState("");

  const tokenRecuperacao = new URLSearchParams(
    window.location.search
  ).get("token");


useEffect(() => {
  function atualizarPaginaRecuperacao() {
    setPaginaRecuperacao(window.location.pathname);
    setErroRecuperacao("");
    setSucessoRecuperacao("");
  }

  window.addEventListener("popstate", atualizarPaginaRecuperacao);

  return () => {
    window.removeEventListener("popstate", atualizarPaginaRecuperacao);
  };
}, []);

  const [aceitouTermos, setAceitouTermos] = useState(false);

  const [erroAuth, setErroAuth] = useState("");
  const [sucessoAuth, setSucessoAuth] = useState("");
  const [processandoAuth, setProcessandoAuth] = useState(false);

  const [modalLegal, setModalLegal] = useState(null);

  const [avaliacaoId, setAvaliacaoId] = useState(null);
  const [questoes, setQuestoes] = useState([]);
  const [respostas, setRespostas] = useState({});
  const [resultado, setResultado] = useState(null);

  const [avaliacoesDisponiveis, setAvaliacoesDisponiveis] = useState([]);
  const [avaliacaoSelecionada, setAvaliacaoSelecionada] = useState(null);
  const [carregandoAvaliacoes, setCarregandoAvaliacoes] = useState(false);

  const [historico, setHistorico] = useState([]);
  const [carregandoHistorico, setCarregandoHistorico] = useState(false);
  const [erroHistorico, setErroHistorico] = useState("");

  const [desempenhosAssunto, setDesempenhosAssunto] = useState([]);
  const [carregandoDesempenhos, setCarregandoDesempenhos] = useState(false);
  const [erroDesempenhos, setErroDesempenhos] = useState("");

  const [historicoAberto, setHistoricoAberto] = useState(false);
  const [dashboardAberto, setDashboardAberto] = useState(false);
  const [paginaAtiva, setPaginaAtiva] = useState("dashboard");

  const [abaCentral, setAbaCentral] = useState("notificacoes");
  const [preferenciasNotificacoes, setPreferenciasNotificacoes] = useState({
  notificacoesAtivas: true,
  lembretesPlano: true,
  metasPendentes: true,
  recomendacoesRevisao: true,
  avaliacoesProximas: false,
  evolucaoDesempenho: true,
  resumoSemanal: false,
  antecedenciaLembretes: 1
});

useEffect(() => {
  if (usuario?.tipo !== "PROFESSOR" || !["disciplinas", "avaliacoes"].includes(paginaProfessor)) {
    return;
  }

  let ativo = true;

  async function carregarConteudoProfessor() {
    setCarregandoConteudoProfessor(true);
    setErroConteudoProfessor("");

    try {
      const [respostaDisciplinas, respostaAssuntos] = await Promise.all([
        fetchProtegido("/api/disciplinas", {
          credentials: "include"
        }),
        fetchProtegido("/api/assuntos", {
          credentials: "include"
        })
      ]);

      if (!respostaDisciplinas.ok || !respostaAssuntos.ok) {
        throw new Error("Não foi possível carregar disciplinas e assuntos.");
      }

      const [disciplinas, assuntos] = await Promise.all([
        respostaDisciplinas.json(),
        respostaAssuntos.json()
      ]);

      if (ativo) {
        setDisciplinasProfessor(disciplinas);
        setAssuntosProfessor(assuntos);
      }
    } catch (erro) {
      if (ativo) {
        setErroConteudoProfessor(erro.message);
      }
    } finally {
      if (ativo) {
        setCarregandoConteudoProfessor(false);
      }
    }
  }

  carregarConteudoProfessor();

  return () => {
    ativo = false;
  };
}, [usuario?.id, usuario?.tipo, paginaProfessor]);


useEffect(() => {
  if (usuario?.tipo !== "PROFESSOR" || paginaProfessor !== "cursos") {
    return;
  }

  let ativo = true;

  async function carregarCursosESemestres() {
    setCarregandoCursosProfessor(true);
    setErroCursosProfessor("");

    try {
      const respostaCursos = await fetchProtegido("/api/cursos", {
        credentials: "include",
      });

      if (!respostaCursos.ok) {
        throw new Error("Não foi possível carregar os cursos.");
      }

      const cursos = await respostaCursos.json();

      const resultadosSemestres = await Promise.all(
        cursos.map(async (curso) => {
          const resposta = await fetchProtegido(
            `/api/semestres-cursos/curso/${curso.id}`,
            {
              credentials: "include",
            }
          );

          if (!resposta.ok) {
            throw new Error(
              `Não foi possível carregar os semestres de ${curso.nome}.`
            );
          }

          return resposta.json();
        })
      );

      if (!ativo) {
        return;
      }

      setCursosProfessor(cursos);

      setSemestresProfessor(
        resultadosSemestres.flat().sort((a, b) => a.numero - b.numero)
      );
    } catch (erro) {
      if (ativo) {
        setErroCursosProfessor(
          erro.message || "Erro ao carregar cursos e semestres."
        );
      }
    } finally {
      if (ativo) {
        setCarregandoCursosProfessor(false);
      }
    }
  }

  carregarCursosESemestres();

  return () => {
    ativo = false;
  };
}, [usuario?.id, usuario?.tipo, paginaProfessor]);


useEffect(() => {
  if (!usuario?.id || usuario?.tipo !== "ESTUDANTE") {
    return;
  }

  async function carregarPreferenciasNotificacoes() {
    try {
      const resposta = await fetchProtegido("/api/preferencias-notificacoes", {
        credentials: "include"
      });

      if (!resposta.ok) {
        throw new Error("Não foi possível carregar as preferências.");
      }

      const dados = await resposta.json();
      setPreferenciasNotificacoes(dados);
    } catch (erro) {
      console.error("Erro ao carregar preferências:", erro);
    }
  }

  carregarPreferenciasNotificacoes();
}, [usuario?.id, usuario?.tipo]);

useEffect(() => {
  if (usuario?.tipo !== "ADMIN") {
    return;
  }

  let ativo = true;

  async function carregarResumoAdmin() {
    setCarregandoResumoAdmin(true);
    setErroResumoAdmin("");

    try {
      const resposta = await fetchProtegido("/api/admin/resumo", {
        credentials: "include"
      });

      if (!resposta.ok) {
        throw new Error("Não foi possível carregar os indicadores administrativos.");
      }

      const dados = await resposta.json();

      if (ativo) {
        setResumoAdmin({
          totalAlunos: dados.totalAlunos,
          totalProfessores: dados.totalProfessores
        });
      }
    } catch (erro) {
      if (ativo) {
        setErroResumoAdmin(erro.message);
      }
    } finally {
      if (ativo) {
        setCarregandoResumoAdmin(false);
      }
    }
  }

  carregarResumoAdmin();

  return () => {
    ativo = false;
  };
}, [usuario?.id, usuario?.tipo]);
useEffect(() => {
  if (usuario?.tipo !== "ADMIN") {
    return;
  }

  let ativo = true;

  async function carregarUsuariosAdmin() {
    setCarregandoUsuariosAdmin(true);
    setErroUsuariosAdmin("");

    try {
      const resposta = await fetchProtegido("/api/admin/usuarios", {
        credentials: "include"
      });

      if (!resposta.ok) {
        throw new Error("Não foi possível carregar os usuários.");
      }

      const dados = await resposta.json();

      if (ativo) {
        setUsuariosAdmin(dados);
      }
    } catch (erro) {
      if (ativo) {
        setErroUsuariosAdmin(erro.message);
      }
    } finally {
      if (ativo) {
        setCarregandoUsuariosAdmin(false);
      }
    }
  }

  carregarUsuariosAdmin();

  return () => {
    ativo = false;
  };
}, [usuario?.id, usuario?.tipo]);
async function cadastrarProfessorAdmin(evento) {
  evento.preventDefault();

  if (cadastrandoProfessorAdmin) {
    return;
  }

  if (senhaProfessorAdmin !== confirmarSenhaProfessorAdmin) {
  setErroCadastroProfessorAdmin("As senhas não coincidem.");
  setSucessoCadastroProfessorAdmin("");
  return;
}

  setCadastrandoProfessorAdmin(true);
  setErroCadastroProfessorAdmin("");
  setSucessoCadastroProfessorAdmin("");

  try {
    const resposta = await fetchProtegido("/api/admin/professores", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        nome: nomeProfessorAdmin.trim(),
        email: emailProfessorAdmin.trim(),
        senha: senhaProfessorAdmin
      })
    });

    if (!resposta.ok) {
      if (resposta.status === 409) {
        throw new Error("Este e-mail já está cadastrado no SAP.");
      }

      throw new Error("Não foi possível cadastrar o professor.");
    }

    const professorCadastrado = await resposta.json();

    setUsuariosAdmin((usuariosAnteriores) => [
      ...usuariosAnteriores,
      professorCadastrado
    ]);

    setResumoAdmin((resumoAnterior) => ({
      ...resumoAnterior,
      totalProfessores: resumoAnterior.totalProfessores + 1
    }));

    setNomeProfessorAdmin("");
setEmailProfessorAdmin("");
setSenhaProfessorAdmin("");
setConfirmarSenhaProfessorAdmin("");
setSucessoCadastroProfessorAdmin("Professor cadastrado com sucesso!");
    setTimeout(() => {
      setSucessoCadastroProfessorAdmin("");
    }, 5000);
  } catch (erro) {
    setErroCadastroProfessorAdmin(erro.message);
  } finally {
    setCadastrandoProfessorAdmin(false);
  }
}

async function alterarAcessoUsuarioAdmin(idUsuario, acessoAtual) {
  setAlterandoAcessoAdmin(idUsuario);
  setErroAlteracaoAcessoAdmin("");

  try {
    const resposta = await fetchProtegido(
      `/api/admin/usuarios/${idUsuario}/acesso`,
      {
        method: "PATCH",
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          ativo: !acessoAtual
        })
      }
    );

    if (!resposta.ok) {
      throw new Error("Não foi possível alterar o acesso do usuário.");
    }

    const dados = await resposta.json();

    setUsuariosAdmin((usuariosAnteriores) =>
      usuariosAnteriores.map((usuarioItem) =>
        usuarioItem.id === idUsuario
          ? { ...usuarioItem, ativo: dados.ativo }
          : usuarioItem
      )
    );
  } catch (erro) {
    setErroAlteracaoAcessoAdmin(erro.message);
  } finally {
    setAlterandoAcessoAdmin(null);
  }
}

async function salvarPreferenciasNotificacoes(novasPreferencias) {
  try {
    const resposta = await fetchProtegido("/api/preferencias-notificacoes", {
      method: "PUT",
      credentials: "include",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(novasPreferencias)
    });

    if (!resposta.ok) {
      throw new Error("Não foi possível salvar as preferências.");
    }

    const dados = await resposta.json();
    setPreferenciasNotificacoes(dados);

    return true;
  } catch (erro) {
    console.error("Erro ao salvar preferências:", erro);
    return false;
  }
}


  const [metasEstudo, setMetasEstudo] = useState([]);
  const [carregandoMetas, setCarregandoMetas] = useState(false);
  const [erroMetas, setErroMetas] = useState("");

  const [notificacoes, setNotificacoes] = useState([]);
  const [carregandoNotificacoes, setCarregandoNotificacoes] = useState(false);
  const [erroNotificacoes, setErroNotificacoes] = useState("");
  const [quantidadeNaoLidas, setQuantidadeNaoLidas] = useState(0);


  const totalMetas = metasEstudo.length;

  const metasConcluidas = metasEstudo.filter(
    (meta) => meta.concluida === true
  ).length;

  const progressoMetas = totalMetas > 0
    ? Math.round((metasConcluidas / totalMetas) * 100)
    : 0;

const hojePlano = new Date();

const inicioSemanaPlano = new Date(
    hojePlano.getFullYear(),
    hojePlano.getMonth(),
    hojePlano.getDate() - ((hojePlano.getDay() + 6) % 7)
);

const diasSemanaPlano = Array.from({ length: 7 }, (_, indice) => {
    const data = new Date(inicioSemanaPlano);
    data.setDate(inicioSemanaPlano.getDate() + indice);

    const dataFormatada = [
        data.getFullYear(),
        String(data.getMonth() + 1).padStart(2, "0"),
        String(data.getDate()).padStart(2, "0")
    ].join("-");

    const metasDoDia = metasEstudo.filter(
        (meta) => meta.dataPrevista === dataFormatada
    );

    return {
        data: dataFormatada,
        dia: ["Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"][indice],
        numero: String(data.getDate()).padStart(2, "0"),
        metas: metasDoDia,
        possuiMetas: metasDoDia.length > 0,
        ehHoje:
            data.toDateString() === hojePlano.toDateString()
    };
});

const metasSemanaPlano = diasSemanaPlano.reduce(
    (total, dia) => total + dia.metas.length,
    0
);



  const [avaliacaoAberta, setAvaliacaoAberta] = useState(false);

  const [buscaLivros, setBuscaLivros] = useState("");
  const [livros, setLivros] = useState([]);
  const [buscandoLivros, setBuscandoLivros] = useState(false);
  const [erroLivros, setErroLivros] = useState("");

  const [carregando, setCarregando] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState("");


const carregarMetasEstudo = async () => {
    setCarregandoMetas(true);
    setErroMetas("");

    try {
        const resposta = await fetch(`${API_URL}/api/plano-estudos`, {
            method: "GET",
            credentials: "include"
        });

        if (!resposta.ok) {
            throw new Error("Não foi possível carregar as metas de estudo.");
        }

        const dados = await resposta.json();
        setMetasEstudo(dados);
    } catch (error) {
        setErroMetas(error.message);
    } finally {
        setCarregandoMetas(false);
    }
};

const atualizarConclusaoMeta = async (metaId, concluida) => {
  setErroMetas("");

  try {
    const resposta = await fetchProtegido(
      `${API_URL}/api/plano-estudos/${metaId}/conclusao`,
      {
        method: "PATCH",
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          concluida
        })
      }
    );

    if (!resposta.ok) {
      throw new Error("Não foi possível atualizar a meta.");
    }

    await carregarMetasEstudo();
  } catch (error) {
    setErroMetas("Não foi possível atualizar a meta. Tente novamente.");
  }
};

useEffect(() => {
    if (paginaAtiva !== "plano-estudos" || !usuario) {
        return;
    }

    carregarMetasEstudo();
}, [paginaAtiva, usuario, configuracaoAcademica]);


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
  if (
    usuario?.tipo === "ESTUDANTE" &&
    usuario?.aceitouTermos === true &&
    configuracaoAcademica?.configurado === true
  ) {
    carregarHistorico();
    carregarDesempenhosAssunto();
    return;
  }

  setQuestoes([]);
  setRespostas({});
  setResultado(null);
  setErro("");
  setCarregando(false);
  setDesempenhosAssunto([]);
}, [usuario, configuracaoAcademica?.configurado]);


useEffect(() => {
  if (!cursoSelecionado || !semestreSelecionado) {
    setOfertasDisciplinas([]);
    setDisciplinasSelecionadas([]);
    return;
  }

  async function carregarOfertas() {
    try {
      const parametros = new URLSearchParams({
        cursoId: cursoSelecionado,
        semestre: semestreSelecionado,
      });

      const response = await fetch(
        `${API_URL}/api/ofertas-disciplinas?${parametros}`,
        { credentials: "include" }
      );

      if (!response.ok) {
        throw new Error("Não foi possível carregar as disciplinas.");
      }

      const dados = await response.json();
      setOfertasDisciplinas(dados);
      setDisciplinasSelecionadas(
      configuracaoAcademica &&
        Number(cursoSelecionado) === Number(configuracaoAcademica.cursoId) &&
        Number(semestreSelecionado) === Number(configuracaoAcademica.semestre)
    ? (configuracaoAcademica.ofertasDisciplinasIds ?? [])
        .map(Number)
        .filter((id) =>
          dados.some((oferta) => Number(oferta.id) === id)
        )
    : []
);
    } catch (error) {
      setOfertasDisciplinas([]);
      setErroConfiguracao(error.message);
    }
  }

  carregarOfertas();
}, [cursoSelecionado, semestreSelecionado]);

  async function salvarConfiguracaoAcademica() {
  if (!cursoSelecionado || !semestreSelecionado) {
    setErroConfiguracao("Selecione seu curso e semestre.");
    return;
  }

  if (disciplinasSelecionadas.length === 0) {
    setErroConfiguracao("Selecione pelo menos uma disciplina.");
    return;
  }

  try {
    setSalvandoConfiguracao(true);
    setErroConfiguracao("");

    const response = await fetchProtegido(`${API_URL}/api/configuracao-academica`, {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        cursoId: Number(cursoSelecionado),
        semestre: Number(semestreSelecionado),
        ofertasDisciplinasIds: disciplinasSelecionadas.map(Number),
      }),
    });

    if (!response.ok) {
      throw new Error("Não foi possível salvar sua configuração acadêmica.");
    }

    const respostaConfiguracao = await fetch(
      `${API_URL}/api/configuracao-academica`,
      {
        credentials: "include",
      }
    );

    if (!respostaConfiguracao.ok) {
      throw new Error("Não foi possível atualizar sua matrícula.");
    }

    const configuracao = await respostaConfiguracao.json();

    const respostaOfertas = await fetch(
      `${API_URL}/api/ofertas-disciplinas?cursoId=${configuracao.cursoId}&semestre=${configuracao.semestre}`,
      {
        credentials: "include",
      }
    );

    if (!respostaOfertas.ok) {
      throw new Error("Não foi possível atualizar as disciplinas.");
    }

    const ofertas = await respostaOfertas.json();

    console.log("CONFIGURAÇÃO SALVA:", configuracao);
    console.log("OFERTAS DO CURSO E SEMESTRE:", ofertas);

    const matriculadas = ofertas.filter((oferta) =>
    configuracao.ofertasDisciplinasIds.includes(oferta.id)
  );

    console.log("DISCIPLINAS MATRICULADAS:", matriculadas);

    setConfiguracaoAcademica(configuracao);
    setDisciplinasMatriculadas(matriculadas);
    setDisciplinasSelecionadas(configuracao.ofertasDisciplinasIds);
    setCursoSelecionado(String(configuracao.cursoId));
    setSemestreSelecionado(String(configuracao.semestre));
    setOfertasDisciplinas(ofertas);
    setAvaliacoesDisponiveis([]);
    setAvaliacaoSelecionada(null);
    setAvaliacaoId(null);
    setQuestoes([]);
    setRespostas({});
    setResultado(null);
  } catch (error) {
    setErroConfiguracao(error.message);
  } finally {
    setSalvandoConfiguracao(false);
  }
}

useEffect(() => {
  if (usuario?.tipo !== "ESTUDANTE" || !usuario?.aceitouTermos) {
  return;
}

  async function carregarCursos() {
    try {
      setErroConfiguracao("");

      const response = await fetch(`${API_URL}/api/cursos`, {
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Não foi possível carregar os cursos.");
      }

      const dados = await response.json();
      setCursos(dados);
    } catch (error) {
      setErroConfiguracao(error.message);
    }
  }

  carregarCursos();
}, [usuario?.tipo, configuracaoAcademica?.configurado]);

async function aceitarTermosOAuth() {
  try {
    setProcessandoAuth(true);
    setErroAuth("");

    const response = await fetchProtegido(
      `${API_URL}/api/auth/aceitar-termos`,
      {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          aceitouTermos: true,
        }),
      }
    );

    if (!response.ok) {
      let mensagem =
        "Não foi possível registrar o aceite dos termos.";

      try {
        const dadosErro = await response.json();

        mensagem =
          dadosErro.erro ||
          dadosErro.mensagem ||
          dadosErro.message ||
          mensagem;
      } catch {
      }

      setErroAuth(mensagem);
      return;
    }

    await verificarSessao();
    setAceitouTermos(false);
  } catch (error) {
    console.error("Erro ao aceitar termos:", error);

    setErroAuth(
      "Não foi possível registrar o aceite. Tente novamente."
    );
  } finally {
    setProcessandoAuth(false);
  }
}
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
      if (dados.tipo === "ESTUDANTE") {
  setVerificandoConfiguracao(true);

  try {
    const respostaConfiguracao = await fetch(
      `${API_URL}/api/configuracao-academica`,
      {
        method: "GET",
        credentials: "include",
      }
    );
    if (!respostaConfiguracao.ok) {
      throw new Error("Não foi possível consultar a configuração acadêmica.");
    }

    const configuracao = await respostaConfiguracao.json();

    setConfiguracaoAcademica(configuracao);
    setCursoSelecionado(String(configuracao.cursoId ?? ""));
    setSemestreSelecionado(String(configuracao.semestre ?? ""));
    setDisciplinasSelecionadas(configuracao.ofertasDisciplinasIds ?? []);

    if (configuracao.configurado === true) {
    const respostaOfertas = await fetch(
    `${API_URL}/api/ofertas-disciplinas?cursoId=${configuracao.cursoId}&semestre=${configuracao.semestre}`,
    {
      credentials: "include",
    }
  );

  if (!respostaOfertas.ok) {
    throw new Error("Não foi possível carregar as disciplinas matriculadas.");
  }

  const ofertas = await respostaOfertas.json();

  setOfertasDisciplinas(ofertas);

  const matriculadas = ofertas.filter((oferta) =>
    configuracao.ofertasDisciplinasIds.includes(oferta.id)
  );

  setDisciplinasMatriculadas(matriculadas);
  console.log("Disciplinas matriculadas:", matriculadas);
} else {
  setDisciplinasMatriculadas([]);
}
  } catch (erro) {
    console.error("Erro ao consultar configuração acadêmica:", erro);
    setConfiguracaoAcademica(null);
  } finally {
    setVerificandoConfiguracao(false);
  }
}
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

      const response = await fetchProtegido(`${API_URL}/api/auth/login`, {
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

      await verificarSessao();
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

      const response = await fetchProtegido(`${API_URL}/api/auth/cadastro`, {
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
          aceitouTermos,
        }),
      });

      if (!response.ok) {
        let mensagem = "Não foi possível criar a conta.";

        try {
          const dadosErro = await response.json();

          mensagem =
            dadosErro.erro ||
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
      await fetchProtegido(`${API_URL}/api/auth/logout`, {
        method: "POST",
        credentials: "include",
      });
    } catch (error) {
      console.error("Erro ao encerrar sessão:", error);
    } finally {
      setUsuario(null);
      setConfiguracaoAcademica(null);
      setVerificandoConfiguracao(false);
      setQuestoes([]);
      setRespostas({});
      setResultado(null);
      setErro("");
      setErroAuth("");
    }
  }

  useEffect(() => {
  if (
    paginaAtiva !== "avaliacoes" ||
    usuario?.tipo !== "ESTUDANTE" ||
    !configuracaoAcademica?.configurado
  ) {
    return;
  }

  carregarAvaliacoes();
}, [paginaAtiva, usuario?.id, configuracaoAcademica]);


useEffect(() => {
  if (paginaAtiva === "central-estudante" && usuario?.tipo === "ESTUDANTE") {
    carregarNotificacoes();
  }
}, [paginaAtiva, usuario?.id, usuario?.tipo]);


useEffect(() => {
  if (usuario?.id && usuario?.tipo === "ESTUDANTE") {
    carregarContadorNotificacoes();
  } else {
    setQuantidadeNaoLidas(0);
  }
}, [usuario?.id, usuario?.tipo]);

async function carregarContadorNotificacoes() {
  try {
    const resposta = await fetch(
      `${API_URL}/api/notificacoes/nao-lidas`,
      {
        credentials: "include"
      }
    );

    if (resposta.status === 401) {
      setQuantidadeNaoLidas(0);
      return;
    }

    if (!resposta.ok) {
      throw new Error("Não foi possível consultar as notificações.");
    }

    const dados = await resposta.json();

    setQuantidadeNaoLidas(dados.quantidade);
  } catch (erro) {
    console.error("Erro ao carregar contador de notificações:", erro);
  }
}

async function carregarNotificacoes() {
  setCarregandoNotificacoes(true);
  setErroNotificacoes("");

  try {
    const [respostaNotificacoes, respostaContagem] = await Promise.all([
      fetch(`${API_URL}/api/notificacoes`, {
        credentials: "include"
      }),
      fetch(`${API_URL}/api/notificacoes/nao-lidas`, {
        credentials: "include"
      })
    ]);

    if (respostaNotificacoes.status === 401 || respostaContagem.status === 401) {
      setNotificacoes([]);
      setQuantidadeNaoLidas(0);
      setErroNotificacoes("Sua sessão expirou. Faça login novamente.");
      return;
    }

    if (!respostaNotificacoes.ok || !respostaContagem.ok) {
      throw new Error("Não foi possível carregar as notificações.");
    }

    const [dadosNotificacoes, dadosContagem] = await Promise.all([
      respostaNotificacoes.json(),
      respostaContagem.json()
    ]);

    setNotificacoes(dadosNotificacoes);
    setQuantidadeNaoLidas(dadosContagem.quantidade);
  } catch (erro) {
    setErroNotificacoes(erro.message);
  } finally {
    setCarregandoNotificacoes(false);
  }
}

async function marcarNotificacaoComoLida(notificacaoId) {
  try {
    const resposta = await fetchProtegido(
      `${API_URL}/api/notificacoes/${notificacaoId}/ler`,
      {
        method: "PATCH",
        credentials: "include"
      }
    );

    if (resposta.status === 401) {
      setErroNotificacoes("Sua sessão expirou. Faça login novamente.");
      return;
    }

    if (!resposta.ok) {
      throw new Error("Não foi possível marcar a notificação como lida.");
    }

    setNotificacoes((anteriores) =>
      anteriores.map((notificacao) =>
        notificacao.id === notificacaoId
          ? { ...notificacao, lida: true }
          : notificacao
      )
    );

    await carregarContadorNotificacoes();
  } catch (erro) {
    setErroNotificacoes(erro.message);
  }
}

async function carregarAssuntos(disciplinaId) {
  if (disciplinaAssuntosAberta === disciplinaId) {
    setDisciplinaAssuntosAberta(null);
    return;
  }

  setDisciplinaAssuntosAberta(disciplinaId);

  if (assuntosPorDisciplina[disciplinaId]) {
    return;
  }

  setCarregandoAssuntos(disciplinaId);

  try {
    const resposta = await fetch(
      `${API_URL}/api/assuntos/disciplina/${disciplinaId}`,
      { credentials: "include" }
    );

    if (!resposta.ok) {
      throw new Error("Erro ao carregar assuntos.");
    }

    const dados = await resposta.json();

    setAssuntosPorDisciplina((anteriores) => ({
      ...anteriores,
      [disciplinaId]: dados,
    }));
  } catch (erro) {
    console.error("Erro ao carregar assuntos:", erro);
    setAssuntosPorDisciplina((anteriores) => ({
      ...anteriores,
      [disciplinaId]: [],
    }));
  } finally {
    setCarregandoAssuntos(null);
  }
}

async function carregarAvaliacoes() {
  try {
    setCarregandoAvaliacoes(true);
    setErro("");

    const response = await fetch(
      `${API_URL}/api/avaliacoes`,
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
      throw new Error("Erro ao buscar avaliações.");
    }

    const dados = await response.json();
    console.log("AVALIAÇÕES RETORNADAS:", dados);
    setAvaliacoesDisponiveis(dados);
  } catch (error) {
    console.error(error);
    setErro("Não foi possível carregar as avaliações.");
    setAvaliacoesDisponiveis([]);
  } finally {
    setCarregandoAvaliacoes(false);
  }
}

  async function carregarQuestoes(idAvaliacao) {
  try {
    setCarregando(true);
    setErro("");
    setQuestoes([]);
    setRespostas({});

    const response = await fetch(
      `${API_URL}/api/questoes/avaliacao/${idAvaliacao}`,
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

    setAvaliacaoId(idAvaliacao);
    setQuestoes(dados);
  } catch (error) {
    console.error(error);
    setErro("Não foi possível carregar a avaliação.");
  } finally {
    setCarregando(false);
  }
}

function selecionarAvaliacao(avaliacao) {
  setAvaliacaoSelecionada(avaliacao);
  setAvaliacaoId(avaliacao.id);
  setQuestoes([]);
  setRespostas({});
  setResultado(null);
  setErro("");

  carregarQuestoes(avaliacao.id);
}

function voltarParaAvaliacoes() {
  setAvaliacaoSelecionada(null);
  setAvaliacaoId(null);
  setQuestoes([]);
  setRespostas({});
  setResultado(null);
  setErro("");
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

      const response = await fetchProtegido(
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


  async function carregarHistorico() {
    setCarregandoHistorico(true);
    setErroHistorico("");

    try {
      const resposta = await fetch(
        `${API_URL}/api/resultados/meu-historico`,
        {
          credentials: "include",
        }
      );

      if (!resposta.ok) {
        throw new Error("Não foi possível carregar o histórico.");
      }

      const dados = await resposta.json();

      if (!Array.isArray(dados)) {
        throw new Error("Formato de histórico inválido.");
      }

      setHistorico(dados);
    } catch (error) {
      console.error("Erro ao carregar histórico:", error);
      setErroHistorico(error.message);
      setHistorico([]);
    } finally {
      setCarregandoHistorico(false);
    }
  }

async function carregarDesempenhosAssunto() {
  setCarregandoDesempenhos(true);
  setErroDesempenhos("");

  try {
    const resposta = await fetch(
      `${API_URL}/api/resultados/meu-desempenho-assuntos`,
      {
        credentials: "include",
      }
    );

    if (!resposta.ok) {
      throw new Error("Não foi possível carregar o desempenho por assunto.");
    }

    const dados = await resposta.json();

    if (!Array.isArray(dados)) {
      throw new Error("Formato de desempenho inválido.");
    }

    setDesempenhosAssunto(dados);
  } catch (error) {
    console.error("Erro ao carregar desempenho por assunto:", error);
    setErroDesempenhos(error.message);
    setDesempenhosAssunto([]);
  } finally {
    setCarregandoDesempenhos(false);
  }
}


    const historicoFiltrado = historico.filter((item) =>
  disciplinasMatriculadas.some(
    (disciplina) =>
      Number(disciplina.disciplinaId) === Number(item.disciplinaId)
  )
);
const desempenhosFiltrados = desempenhosAssunto.filter((item) =>
  disciplinasMatriculadas.some(
    (disciplina) =>
      Number(disciplina.disciplinaId) === Number(item.disciplinaId)
  )
);

const desempenhosMaisRecentes = Array.from(
  new Map(
    [...desempenhosFiltrados]
      .sort(
        (a, b) =>
          new Date(a.dataRealizacao).getTime() -
          new Date(b.dataRealizacao).getTime() ||
          Number(a.resultadoId) - Number(b.resultadoId)
      )
      .map((item) => [
        item.assuntoId,
        item,
      ])
  ).values()
);


const evolucaoPorAssunto = desempenhosMaisRecentes.map((desempenhoAtual) => {
  const tentativas = desempenhosFiltrados
    .filter(
      (item) =>
        Number(item.assuntoId) === Number(desempenhoAtual.assuntoId)
    )
    .sort(
      (a, b) =>
        new Date(b.dataRealizacao).getTime() -
          new Date(a.dataRealizacao).getTime() ||
        Number(b.resultadoId) - Number(a.resultadoId)
    );

  const tentativaAnterior = tentativas[1] || null;

  const percentualAtual = Number(desempenhoAtual.percentual || 0);
  const percentualAnterior = tentativaAnterior
    ? Number(tentativaAnterior.percentual || 0)
    : null;

  const diferenca =
    percentualAnterior !== null
      ? percentualAtual - percentualAnterior
      : null;

  const tendencia =
    diferenca === null
      ? "PRIMEIRA"
      : diferenca > 0
      ? "MELHOROU"
      : diferenca < 0
      ? "PIOROU"
      : "MANTEVE";

  return {
    ...desempenhoAtual,
    percentualAnterior,
    diferenca,
    tendencia,
  };
});

const assuntoRecomendado = [...desempenhosMaisRecentes]
  .filter((item) => Number.isFinite(Number(item.percentual)))
  .sort((a, b) => {
    const ordemPrioridade = {
      ALTA: 0,
      MEDIA: 1,
      BAIXA: 2,
    };

    const prioridadeA = ordemPrioridade[a.prioridade] ?? 3;
    const prioridadeB = ordemPrioridade[b.prioridade] ?? 3;

    return (
      prioridadeA - prioridadeB ||
      Number(a.percentual) - Number(b.percentual)
    );
  })[0] || null;

const assuntosPrioridadeAlta = desempenhosMaisRecentes.filter(
  (item) => item.prioridade === "ALTA"
).length;

const assuntosPrioridadeMedia = desempenhosMaisRecentes.filter(
  (item) => item.prioridade === "MEDIA"
).length;

  const totalAvaliacoes = historicoFiltrado.length;

  const desempenhoMedio =
  totalAvaliacoes > 0
    ? historicoFiltrado.reduce(
        (soma, item) => soma + Number(item.percentual || 0),
        0
      ) / totalAvaliacoes
    : 0;

  const melhorDesempenho =
  totalAvaliacoes > 0
    ? Math.max(
        ...historicoFiltrado.map((item) => Number(item.percentual || 0))
      )
    : 0;

const evolucaoDesempenho = [...historicoFiltrado]
  .sort(
    (a, b) =>
      new Date(a.dataRealizacao).getTime() -
        new Date(b.dataRealizacao).getTime() ||
      Number(a.resultadoId) - Number(b.resultadoId)
  )
  .map((item, index) => ({
    numero: index + 1,
    resultadoId: item.resultadoId,
    percentual: Number(item.percentual || 0),
    acertos: Number(item.acertos || 0),
    totalQuestoes: Number(item.totalQuestoes || 0),
    data: item.dataRealizacao,
  }));

const evolucaoPorDisciplina = disciplinasMatriculadas
  .map((disciplina) => {
    const avaliacoes = historicoFiltrado
      .filter(
        (item) =>
          Number(item.disciplinaId) ===
          Number(disciplina.disciplinaId)
      )
      .sort(
        (a, b) =>
          new Date(a.dataRealizacao).getTime() -
            new Date(b.dataRealizacao).getTime() ||
          Number(a.resultadoId) - Number(b.resultadoId)
      )
      .map((item, index) => ({
        numero: index + 1,
        resultadoId: item.resultadoId,
        percentual: Number(item.percentual || 0),
        data: item.dataRealizacao,
      }));

    return {
      disciplinaId: disciplina.disciplinaId,
      disciplinaNome: disciplina.disciplinaNome,
      avaliacoes,
    };
  })
  .filter((disciplina) => disciplina.avaliacoes.length > 0);

  const ultimasAvaliacoes = historicoFiltrado.slice(0, 3);

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

      const response = await fetchProtegido(
  `${API_URL}/api/avaliacoes/finalizar`,
  {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      usuarioId: usuario.id,
      avaliacaoId: avaliacaoId,
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

      await Promise.all([
      carregarHistorico(),
      carregarDesempenhosAssunto(),
    ]);
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

 if (usuario && usuario.aceitouTermos === false) {
  return (
    <div className="auth-page">
      <section className="auth-area">
        <div className="auth-card">
          <div className="auth-cabecalho">
            <span className="auth-etiqueta">
              Primeiro acesso
            </span>

            <h2>Termos de Uso e Privacidade</h2>

            <p>
              Para continuar utilizando o SAP, você precisa ler e
              concordar com os Termos de Uso e a Política de Privacidade.
            </p>
          </div>

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
                onClick={() => setModalLegal("termos")}
              >
                Termos de Uso
              </button>{" "}
              e a{" "}
              <button
                type="button"
                onClick={() => setModalLegal("privacidade")}
              >
                Política de Privacidade
              </button>
              .
            </span>
          </label>

          {erroAuth && (
            <div className="mensagem mensagem-erro">
              {erroAuth}
            </div>
          )}

          <button
            className="botao-principal"
            type="button"
            disabled={!aceitouTermos || processandoAuth}
            onClick={aceitarTermosOAuth}
          >
            {processandoAuth
              ? "Aguarde..."
              : "Aceitar e continuar"}
          </button>
        </div>
      </section>

      {renderModalLegal()}
    </div>
  );
}

if (!usuario && paginaRecuperacao === "/redefinir-senha") {
  return (
    <div className="auth-page">
      <section className="auth-brand">
        <div className="brand-conteudo">
          <div className="logo-marca logo-claro">SAP</div>
          <span className="brand-badge">
            Aprendizagem personalizada
          </span>
          <h1>Crie uma nova senha.</h1>
          <p>Recupere seu acesso e continue seus estudos.</p>
        </div>
      </section>

      <section className="auth-formulario">
        <div className="auth-card">
          <h2>Redefinir senha</h2>
          <p>Informe e confirme sua nova senha.</p>

          <form
            onSubmit={async (event) => {
              event.preventDefault();
              setErroRecuperacao("");
              setSucessoRecuperacao("");

              if (!tokenRecuperacao) {
                setErroRecuperacao("Link de recuperação inválido.");
                return;
              }

              if (novaSenhaRecuperacao.length < 8 || novaSenhaRecuperacao.length > 72) {
                setErroRecuperacao(
                  "A senha deve conter entre 8 e 72 caracteres."
                );
                return;
              }

              if (novaSenhaRecuperacao !== confirmarSenhaRecuperacao) {
                setErroRecuperacao("As senhas não coincidem.");
                return;
              }

              setCarregandoRecuperacao(true);

              try {
                const respostaCsrf = await fetch(
                  `${API_URL}/api/auth/csrf`,
                  {
                    credentials: "include"
                  }
                );

                if (!respostaCsrf.ok) {
                  throw new Error("Não foi possível obter a proteção CSRF.");
                }

                const dadosCsrf = await respostaCsrf.json();

                const resposta = await fetch(
                  `${API_URL}/api/auth/redefinir-senha`,
                  {
                    method: "POST",
                    credentials: "include",
                    headers: {
                      "Content-Type": "application/json",
                      [dadosCsrf.headerName || "X-XSRF-TOKEN"]: dadosCsrf.token
                    },
                    body: JSON.stringify({
                      token: tokenRecuperacao,
                      novaSenha: novaSenhaRecuperacao
                    })
                  }
                );

                const dados = await resposta.json();

                if (!resposta.ok) {
                  throw new Error(
                    dados.mensagem || "Não foi possível redefinir a senha."
                  );
                }

                setNovaSenhaRecuperacao("");
                setConfirmarSenhaRecuperacao("");
                setSucessoRecuperacao("Senha redefinida com sucesso.");
                window.history.replaceState({}, "", "/redefinir-senha");
              } catch (erro) {
                setErroRecuperacao(
                  erro.message || "Não foi possível redefinir a senha."
                );
              } finally {
                setCarregandoRecuperacao(false);
              }
            }}
          >
            <div className="campo">
              <label htmlFor="nova-senha-recuperacao">Nova senha</label>
              <input
                id="nova-senha-recuperacao"
                type="password"
                autoComplete="new-password"
                placeholder="Digite sua nova senha"
                value={novaSenhaRecuperacao}
                onChange={(event) =>
                  setNovaSenhaRecuperacao(event.target.value)
                }
                minLength={8}
                maxLength={72}
                required
              />
            </div>

            <div className="campo">
              <label htmlFor="confirmar-senha-recuperacao">
                Confirmar nova senha
              </label>
              <input
                id="confirmar-senha-recuperacao"
                type="password"
                autoComplete="new-password"
                placeholder="Confirme sua nova senha"
                value={confirmarSenhaRecuperacao}
                onChange={(event) =>
                  setConfirmarSenhaRecuperacao(event.target.value)
                }
                minLength={8}
                maxLength={72}
                required
              />
            </div>

            {erroRecuperacao && (
              <div className="mensagem mensagem-erro">
                {erroRecuperacao}
              </div>
            )}

            {sucessoRecuperacao && (
              <div className="mensagem mensagem-sucesso">
                {sucessoRecuperacao}
              </div>
            )}

            <button
              type="submit"
              disabled={carregandoRecuperacao || !!sucessoRecuperacao}
            >
              {carregandoRecuperacao
                ? "Redefinindo..."
                : "Redefinir senha"}
            </button>
          </form>

          <button
            type="button"
            onClick={() => {
              setNovaSenhaRecuperacao("");
              setConfirmarSenhaRecuperacao("");
              window.history.pushState({}, "", "/");
              window.dispatchEvent(new PopStateEvent("popstate"));
            }}
          >
            Voltar ao login
          </button>
        </div>
      </section>
    </div>
  );
}

if (!usuario && paginaRecuperacao === "/esqueci-senha") {
  return (
    <div className="auth-page">
      <section className="auth-brand">
        <div className="brand-conteudo">
          <div className="logo-marca logo-claro">SAP</div>
          <span className="brand-badge">Aprendizagem personalizada</span>
          <h1>Recupere seu acesso.</h1>
          <p>Vamos ajudar você a voltar aos seus estudos.</p>
        </div>
      </section>

      <section className="auth-formulario">
        <div className="auth-card">
          <h2>Esqueci minha senha</h2>
          <p>
            Informe o e-mail cadastrado no SAP para receber
            as instruções de recuperação.
          </p>

          <form
            onSubmit={async (event) => {
              event.preventDefault();
              setErroRecuperacao("");
              setSucessoRecuperacao("");
              setCarregandoRecuperacao(true);

              try {
                const resposta = await fetch(
                  `${API_URL}/api/auth/esqueci-senha`,
                  {
                    method: "POST",
                    headers: {
                      "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                      email: emailRecuperacao.trim()
                    })
                  }
                );

                if (!resposta.ok) {
                  throw new Error("Não foi possível processar a solicitação.");
                }

                setSucessoRecuperacao(
                  "Se o e-mail estiver cadastrado, você receberá as instruções para recuperar sua senha."
                );
              } catch {
                setErroRecuperacao(
                  "Não foi possível realizar a solicitação. Tente novamente."
                );
              } finally {
                setCarregandoRecuperacao(false);
              }
            }}
          >
            <div className="campo">
              <label htmlFor="email-recuperacao">E-mail</label>
              <input
                id="email-recuperacao"
                type="email"
                autoComplete="email"
                placeholder="seuemail@exemplo.com"
                value={emailRecuperacao}
                onChange={(event) =>
                  setEmailRecuperacao(event.target.value)
                }
                required
              />
            </div>

            {erroRecuperacao && (
              <div className="mensagem mensagem-erro">
                {erroRecuperacao}
              </div>
            )}

            {sucessoRecuperacao && (
              <div className="mensagem mensagem-sucesso">
                {sucessoRecuperacao}
              </div>
            )}

            <button
              type="submit"
              disabled={carregandoRecuperacao}
            >
              {carregandoRecuperacao
                ? "Enviando..."
                : "Enviar link de recuperação"}
            </button>
          </form>

          <button
            type="button"
            onClick={() => {
              window.history.pushState({}, "", "/");
              window.dispatchEvent(new PopStateEvent("popstate"));
            }}
          >
            Voltar ao login
          </button>
        </div>
      </section>
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

                {!cadastro && (
                <div className="recuperacao-senha-link">
                  <button
                    type="button"
                    onClick={() => {
                      window.history.pushState({}, "", "/esqueci-senha");
                      window.dispatchEvent(new PopStateEvent("popstate"));
                    }}
                  >
                    Esqueci minha senha
                  </button>
                </div>
              )}

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

    if (
  usuario?.tipo === "ESTUDANTE" &&
  (verificandoConfiguracao || configuracaoAcademica === null)
) {
  return (
    <div className="tela-carregamento">
      <div className="logo-marca logo-carregamento">
        SAP
      </div>

      <div className="spinner" />

      <p>Carregando sua configuração acadêmica...</p>
    </div>
  );
}
if (
  usuario?.tipo === "ESTUDANTE" &&
  configuracaoAcademica?.configurado === false
) {
  return (
    <div className="auth-page">
      <section className="auth-main-area">
        <div className="auth-card">
          <div className="auth-cabecalho">
            <span className="auth-etiqueta">Primeiro acesso</span>
            <h2>Configure sua jornada acadêmica</h2>
            <p>
              Selecione seu curso, semestre e disciplinas para personalizar
              sua experiência no SAP.
            </p>
            <div className="auth-campo">
  <label htmlFor="curso-academico">Curso</label>

  <select
    id="curso-academico"
    value={cursoSelecionado}
    onChange={(event) => setCursoSelecionado(event.target.value)}
  >
    <option value="">Selecione seu curso</option>

    {cursos.map((curso) => (
      <option key={curso.id} value={curso.id}>
        {curso.nome}
      </option>
    ))}
  </select>
</div>
<div className="auth-campo">
  <label htmlFor="semestre-academico">Semestre</label>

  <select
    id="semestre-academico"
    value={semestreSelecionado}
    onChange={(event) => setSemestreSelecionado(event.target.value)}
  >
    <option value="">Selecione seu semestre</option>

    {Array.from({ length: 10 }, (_, indice) => indice + 1).map((semestre) => (
      <option key={semestre} value={semestre}>
        {semestre}º semestre
      </option>
    ))}
  </select>
</div>
<div className="auth-campo">
  <label>Disciplinas do semestre</label>

  {!cursoSelecionado || !semestreSelecionado ? (
  <p>Selecione o curso e o semestre para visualizar as disciplinas.</p>
) : ofertasDisciplinas.length === 0 ? (
  <p>Nenhuma disciplina disponível para este curso e semestre.</p>
) : (
  <div className="lista-disciplinas">
    {ofertasDisciplinas.map((oferta) => (
      <label key={oferta.id} className="disciplina-opcao">
        <input
          type="checkbox"
          checked={disciplinasSelecionadas.includes(oferta.id)}
          onChange={(event) => {
            if (event.target.checked) {
              setDisciplinasSelecionadas((anteriores) => [
                ...anteriores,
                oferta.id,
              ]);
            } else {
              setDisciplinasSelecionadas((anteriores) =>
                anteriores.filter((id) => id !== oferta.id)
              );
            }
          }}
        />
        <span>{oferta.disciplinaNome}</span>
      </label>
    ))}
  </div>
)}
</div>
{erroConfiguracao && (
  <p className="mensagem-erro">{erroConfiguracao}</p>
)}

<button
  type="button"
  className="botao-principal"
  onClick={salvarConfiguracaoAcademica}
  disabled={salvandoConfiguracao}
>
  {salvandoConfiguracao ? "Salvando..." : "Salvar e continuar"}
</button>
          </div>
        </div>
      </section>
    </div>
  );
}

    if (usuario?.tipo === "ADMIN") {
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
              {usuario.nome?.charAt(0)?.toUpperCase() || "A"}
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
        <section className="professor-hero">
          <span className="secao-etiqueta">
            Administração do sistema
          </span>

          <h1>Painel Administrativo</h1>

          <p>
            Gerencie os acessos e cadastre professores no SAP.
          </p>
        </section>
        <section className="admin-indicadores">
  <div className="admin-indicador-card">
    <span>ALUNOS CADASTRADOS</span>

    <strong>
      {carregandoResumoAdmin
        ? "..."
        : resumoAdmin.totalAlunos}
    </strong>
  </div>

  <div className="admin-indicador-card">
    <span>PROFESSORES CADASTRADOS</span>

    <strong>
      {carregandoResumoAdmin
        ? "..."
        : resumoAdmin.totalProfessores}
    </strong>
  </div>

  {erroResumoAdmin && (
    <p className="admin-erro">{erroResumoAdmin}</p>
  )}
</section>
<section className="admin-gerenciamento">
  <div className="admin-abas">
    <button
      type="button"
      className={abaAdmin === "acessos" ? "admin-aba ativa" : "admin-aba"}
      onClick={() => setAbaAdmin("acessos")}
    >
      Gerenciar acessos
    </button>

    <button
      type="button"
      className={abaAdmin === "professores" ? "admin-aba ativa" : "admin-aba"}
      onClick={() => setAbaAdmin("professores")}
    >
      Cadastrar professor
    </button>
  </div>

  <div className="admin-conteudo-aba">
    {abaAdmin === "acessos" ? (
  <div className="admin-acessos">
    <div className="admin-acessos-cabecalho">
      <div>
        <h2>Gerenciar acessos</h2>
        <p>Visualize usuários e controle o acesso ao sistema.</p>
      </div>

      <input
        type="text"
        className="admin-busca"
        placeholder="Buscar por nome ou e-mail..."
        value={buscaUsuariosAdmin}
        onChange={(e) => setBuscaUsuariosAdmin(e.target.value)}
      />
    </div>

    {carregandoUsuariosAdmin ? (
      <p>Carregando usuários...</p>
    ) : erroUsuariosAdmin ? (
      <p className="admin-erro">{erroUsuariosAdmin}</p>
    ) : (
  <>
    {erroAlteracaoAcessoAdmin && (
      <p className="admin-erro">{erroAlteracaoAcessoAdmin}</p>
    )}

    <div className="admin-lista-usuarios">
          {[...usuariosAdmin]
      .filter((usuarioItem) => usuarioItem.tipo !== "ADMIN")
      .sort((a, b) => b.id - a.id)
      .filter((usuarioItem) => {
            const busca = buscaUsuariosAdmin.trim().toLowerCase();

            return (
              usuarioItem.nome.toLowerCase().includes(busca) ||
              usuarioItem.email.toLowerCase().includes(busca)
            );
          })
          .map((usuarioItem) => (
            <div className="admin-usuario-card" key={usuarioItem.id}>
              <div className="admin-usuario-info">
                <strong>{usuarioItem.nome}</strong>
                <span>{usuarioItem.email}</span>
                <small>
                  {usuarioItem.tipo === "PROFESSOR"
                    ? "Professor"
                    : "Aluno"}
                </small>
              </div>

              <div className="admin-usuario-acoes">
                <span
                  className={
                    usuarioItem.ativo
                      ? "admin-status ativo"
                      : "admin-status inativo"
                  }
                >
                  {usuarioItem.ativo ? "Ativo" : "Inativo"}
                </span>

                <button
                  type="button"
                  className="admin-botao-acesso"
                  disabled={alterandoAcessoAdmin !== null}
                  onClick={() =>
                    alterarAcessoUsuarioAdmin(usuarioItem.id, usuarioItem.ativo)
                  }
                >
                  {alterandoAcessoAdmin === usuarioItem.id
                    ? "Atualizando..."
                    : usuarioItem.ativo
                      ? "Desativar acesso"
                      : "Ativar acesso"}
                </button>
              </div>
            </div>
          ))}

        {usuariosAdmin
          .filter((usuarioItem) => usuarioItem.tipo !== "ADMIN")
          .filter((usuarioItem) => {
            const busca = buscaUsuariosAdmin.trim().toLowerCase();

            return (
              usuarioItem.nome.toLowerCase().includes(busca) ||
              usuarioItem.email.toLowerCase().includes(busca)
            );
          }).length === 0 && (
          <p>Nenhum usuário encontrado.</p>
        )}
            </div>
    </>
    )}
  </div>
) : (
      <div className="admin-cadastro-professor">
  <div className="admin-cadastro-cabecalho">
    <h2>Cadastrar professor</h2>
    <p>Cadastre novos professores para acessar o SAP.</p>
  </div>

  <form
  className="admin-formulario-professor"
  onSubmit={cadastrarProfessorAdmin}
>
    <div className="admin-campo-professor">
      <label htmlFor="admin-nome-professor">Nome completo</label>
      <input
        id="admin-nome-professor"
        type="text"
        placeholder="Digite o nome completo"
        value={nomeProfessorAdmin}
        onChange={(e) => setNomeProfessorAdmin(e.target.value)}
        required
      />
    </div>

    <div className="admin-campo-professor">
      <label htmlFor="admin-email-professor">E-mail</label>
      <input
        id="admin-email-professor"
        type="email"
        placeholder="Digite o e-mail do professor"
        value={emailProfessorAdmin}
        onChange={(e) => setEmailProfessorAdmin(e.target.value)}
        required
      />
    </div>

    <div className="admin-campo-professor">
      <label htmlFor="admin-senha-professor">Senha inicial</label>
      <input
        id="admin-senha-professor"
        type="password"
        placeholder="Digite uma senha com pelo menos 6 caracteres"
        value={senhaProfessorAdmin}
        onChange={(e) => setSenhaProfessorAdmin(e.target.value)}
        minLength={6}
        required
      />
    </div>
    <div className="admin-campo-professor">
  <label htmlFor="admin-confirmar-senha-professor">
    Confirmar senha
  </label>
  <input
    id="admin-confirmar-senha-professor"
    type="password"
    placeholder="Digite novamente a senha"
    value={confirmarSenhaProfessorAdmin}
    onChange={(e) => setConfirmarSenhaProfessorAdmin(e.target.value)}
    minLength={6}
    required
  />
</div>

    {erroCadastroProfessorAdmin && (
      <p className="admin-erro">{erroCadastroProfessorAdmin}</p>
    )}

    {sucessoCadastroProfessorAdmin && (
      <p className="admin-sucesso">{sucessoCadastroProfessorAdmin}</p>
    )}

    <button
      type="submit"
      className="admin-botao-cadastrar"
      disabled={cadastrandoProfessorAdmin}
    >
      {cadastrandoProfessorAdmin
        ? "Cadastrando..."
        : "Cadastrar professor"}
    </button>
  </form>
</div>
    )}
  </div>
</section>
      </main>

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
          {paginaProfessor === "painel" ? (
  <>
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
              <button
                type="button"
                className="professor-card"
                onClick={() => setPaginaProfessor("disciplinas")}
              >
                <div className="professor-card-conteudo">
                  <h2>Disciplinas e Assuntos</h2>
                  <p>Gerencie as disciplinas e os assuntos do sistema.</p>
                </div>
              </button>

              <button
                type="button"
                className="professor-card"
                onClick={() => setPaginaProfessor("cursos")}
              >
                <div className="professor-card-conteudo">
                  <h2>Cursos e Semestres</h2>
                  <p>Gerencie os cursos e seus semestres.</p>
                </div>

              </button>

              <button
                type="button"
                className="professor-card"
                onClick={() => setPaginaProfessor("avaliacoes")}
              >
                <div className="professor-card-conteudo">
                  <h2>Avaliações</h2>
                  <p>Crie, edite e gerencie as avaliações do sistema.</p>
                </div>
              </button>
            </section>

          <section className="professor-seguranca">
            <div className="professor-seguranca-icone">
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
                      </>
) : paginaProfessor === "disciplinas" ? (
  <section className="professor-gerenciamento">
    <button
      type="button"
      className="professor-voltar"
      onClick={() => setPaginaProfessor("painel")}
    >
      ← Voltar ao painel
    </button>

    <div className="professor-gerenciamento-cabecalho">
      <h1>Disciplinas e Assuntos</h1>
      <p>Gerencie as disciplinas e os assuntos do sistema.</p>
    </div>

    <div className="professor-gerenciamento-acoes">
      <input
      type="text"
      className="professor-gerenciamento-busca"
      placeholder="Buscar disciplina ou assunto..."
      value={buscaConteudoProfessor}
      onChange={(e) => setBuscaConteudoProfessor(e.target.value)}
    />

      <div className="professor-gerenciamento-botoes">
        <button
  type="button"
  className="professor-botao-adicionar"
  onClick={() => {
    setFormularioDisciplinaAberto(!formularioDisciplinaAberto);
    setErroCadastroDisciplina("");
    setSucessoCadastroDisciplina("");
  }}
>
  {formularioDisciplinaAberto ? "Cancelar" : "+ Adicionar Disciplina"}
</button>

        <button
        type="button"
        className="professor-botao-secundario"
        onClick={() => {
          setFormularioAssuntoAberto(!formularioAssuntoAberto);
          setDisciplinaNovoAssunto("");
          setNomeNovoAssunto("");
          setDescricaoNovoAssunto("");
          setErroCadastroAssunto("");
          setSucessoCadastroAssunto("");
        }}
      >
        {formularioAssuntoAberto ? "Cancelar" : "+ Adicionar Assunto"}
      </button>
      </div>
    </div>
    {formularioDisciplinaAberto && (
  <div className="professor-formulario-disciplina">
    <h2>Adicionar nova disciplina</h2>
    <p>Preencha os dados para cadastrar uma disciplina no sistema.</p>

    <form onSubmit={cadastrarDisciplinaProfessor}>
      <div className="professor-formulario-campo">
        <label htmlFor="nome-nova-disciplina">Nome da disciplina</label>
        <input
          id="nome-nova-disciplina"
          type="text"
          placeholder="Ex.: Engenharia de Software"
          value={nomeNovaDisciplina}
          onChange={(e) => setNomeNovaDisciplina(e.target.value)}
          maxLength={100}
          required
        />
      </div>

      <div className="professor-formulario-campo">
        <label htmlFor="descricao-nova-disciplina">Descrição</label>
        <textarea
          id="descricao-nova-disciplina"
          placeholder="Descreva brevemente o conteúdo da disciplina..."
          value={descricaoNovaDisciplina}
          onChange={(e) => setDescricaoNovaDisciplina(e.target.value)}
          maxLength={255}
          rows={3}
        />
      </div>

      {erroCadastroDisciplina && (
        <p className="admin-erro">{erroCadastroDisciplina}</p>
      )}

      {sucessoCadastroDisciplina && (
        <p className="professor-cadastro-sucesso">
          {sucessoCadastroDisciplina}
        </p>
      )}

      <button
        type="submit"
        className="professor-botao-adicionar"
        disabled={salvandoDisciplina}
      >
        {salvandoDisciplina ? "Salvando..." : "Cadastrar Disciplina"}
      </button>
    </form>
  </div>
)}
{disciplinaParaExcluir && (
  <div className="professor-confirmacao-exclusao">
    <h2>Confirmar exclusão</h2>

    <p>
      Tem certeza de que deseja excluir a disciplina{" "}
      <strong>{disciplinaParaExcluir.nome}</strong>?
    </p>

    <p className="professor-confirmacao-aviso">
      Esta ação não poderá ser desfeita. Disciplinas com assuntos ou
      vínculos acadêmicos não poderão ser excluídas.
    </p>

    {erroExclusaoDisciplina && (
      <p className="admin-erro">{erroExclusaoDisciplina}</p>
    )}

    <div className="professor-confirmacao-acoes">
      <button
        type="button"
        className="professor-botao-secundario"
        onClick={() => setDisciplinaParaExcluir(null)}
        disabled={excluindoDisciplina}
      >
        Cancelar
      </button>

      <button
  type="button"
  className="professor-botao-confirmar-exclusao"
  onClick={excluirDisciplinaProfessor}
  disabled={excluindoDisciplina}
>
  {excluindoDisciplina ? "Excluindo..." : "Confirmar exclusão"}
</button>
    </div>
  </div>
)}

{sucessoExclusaoDisciplina && (
  <p className="professor-cadastro-sucesso">
    {sucessoExclusaoDisciplina}
  </p>
)}

{formularioAssuntoAberto && (
  <div className="professor-formulario-disciplina">
    <h2>Adicionar novo assunto</h2>
    <p>Cadastre um assunto vinculado a uma disciplina existente.</p>

    <form onSubmit={cadastrarAssuntoProfessor}>
      <div className="professor-formulario-campo">
        <label htmlFor="disciplina-novo-assunto">
          Disciplina
        </label>

        <select
          id="disciplina-novo-assunto"
          value={disciplinaNovoAssunto}
          onChange={(e) => setDisciplinaNovoAssunto(e.target.value)}
          required
        >
          <option value="">Selecione uma disciplina</option>

          {[...disciplinasProfessor]
            .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"))
            .map((disciplina) => (
              <option key={disciplina.id} value={disciplina.id}>
                {disciplina.nome}
              </option>
            ))}
        </select>
      </div>

      <div className="professor-formulario-campo">
        <label htmlFor="nome-novo-assunto">
          Nome do assunto
        </label>

        <input
          id="nome-novo-assunto"
          type="text"
          placeholder="Ex.: Consultas SQL Avançadas"
          value={nomeNovoAssunto}
          onChange={(e) => setNomeNovoAssunto(e.target.value)}
          maxLength={100}
          required
        />
      </div>

      <div className="professor-formulario-campo">
        <label htmlFor="descricao-novo-assunto">
          Descrição
        </label>

        <textarea
          id="descricao-novo-assunto"
          placeholder="Descreva brevemente o conteúdo do assunto..."
          value={descricaoNovoAssunto}
          onChange={(e) => setDescricaoNovoAssunto(e.target.value)}
          maxLength={255}
          rows={3}
        />
      </div>

      {erroCadastroAssunto && (
        <p className="admin-erro">{erroCadastroAssunto}</p>
      )}

      <button
        type="submit"
        className="professor-botao-adicionar"
        disabled={salvandoAssunto}
      >
        {salvandoAssunto ? "Salvando..." : "Cadastrar Assunto"}
      </button>
    </form>
  </div>
)}

{assuntoParaExcluir && (
  <div className="professor-confirmacao-exclusao">
    <h3>Confirmar exclusão de assunto</h3>

    <p>
      Deseja realmente excluir o assunto{" "}
      <strong>{assuntoParaExcluir.nome}</strong>?
    </p>

    <p>
      A exclusão será bloqueada se o assunto possuir avaliações
      ou questões vinculadas.
    </p>

    {erroExclusaoAssunto && (
      <p className="admin-erro">{erroExclusaoAssunto}</p>
    )}

    <div className="professor-confirmacao-acoes">
      <button
        type="button"
        className="professor-botao-secundario"
        onClick={() => {
          setAssuntoParaExcluir(null);
          setErroExclusaoAssunto("");
        }}
        disabled={excluindoAssunto}
      >
        Cancelar
      </button>

      <button
        type="button"
        className="professor-botao-excluir"
        onClick={excluirAssuntoProfessor}
        disabled={excluindoAssunto}
      >
        {excluindoAssunto ? "Excluindo..." : "Excluir assunto"}
      </button>
    </div>
  </div>
)}

{sucessoExclusaoAssunto && (
  <p className="professor-mensagem-sucesso">
    {sucessoExclusaoAssunto}
  </p>
)}

    <div className="professor-lista-disciplinas">
  {carregandoConteudoProfessor ? (
    <p>Carregando disciplinas...</p>
  ) : erroConteudoProfessor ? (
    <p className="admin-erro">{erroConteudoProfessor}</p>
  ) : (
    disciplinasProfessor
      .filter((disciplina) => {
        const busca = buscaConteudoProfessor.trim().toLowerCase();

        return (
          disciplina.nome.toLowerCase().includes(busca) ||
          assuntosProfessor.some(
            (assunto) =>
              assunto.disciplina?.id === disciplina.id &&
              assunto.nome.toLowerCase().includes(busca)
          )
        );
      })
      .map((disciplina) => {
        const assuntosDaDisciplina = assuntosProfessor.filter(
          (assunto) => assunto.disciplina?.id === disciplina.id
        );

        const expandida = disciplinaExpandidaProfessor === disciplina.id;

        return (
          <div className="professor-disciplina-card" key={disciplina.id}>
            <div className="professor-disciplina-cabecalho">
              <button
                type="button"
                className="professor-disciplina-expandir"
                onClick={() =>
                  setDisciplinaExpandidaProfessor(
                    expandida ? null : disciplina.id
                  )
                }
              >
                <span>{expandida ? "⌄" : "›"}</span>
                <strong>{disciplina.nome}</strong>
              </button>

              <span className="professor-disciplina-contagem">
                {assuntosDaDisciplina.length} assuntos
              </span>

              <button
              type="button"
              className="professor-botao-remover"
              onClick={() => {
                setDisciplinaParaExcluir(disciplina);
                setErroExclusaoDisciplina("");
                setSucessoExclusaoDisciplina("");
              }}
            >
              Remover Disciplina
            </button>
            </div>

            {expandida && (
              <div className="professor-disciplina-assuntos">
                {assuntosDaDisciplina.length === 0 ? (
                  <p>Nenhum assunto cadastrado nesta disciplina.</p>
                ) : (
                  assuntosDaDisciplina.map((assunto) => (
                    <div
                      className="professor-assunto-item"
                      key={assunto.id}
                    >
                      <span>{assunto.nome}</span>

                      <button
                      type="button"
                      className="professor-remover-assunto"
                      onClick={() => {
                        setAssuntoParaExcluir(assunto);
                        setErroExclusaoAssunto("");
                        setSucessoExclusaoAssunto("");
                      }}
                    >
                      Remover
                    </button>
                    </div>
                  ))
                )}

                <button
                type="button"
                className="professor-adicionar-assunto-disciplina"
                onClick={() => {
                  setFormularioAssuntoAberto(true);
                  setDisciplinaNovoAssunto(String(disciplina.id));
                  setNomeNovoAssunto("");
                  setDescricaoNovoAssunto("");
                  setErroCadastroAssunto("");
                  setSucessoCadastroAssunto("");
                  window.scrollTo({ top: 0, behavior: "smooth" });
                }}
              >
                + Adicionar Assunto em {disciplina.nome}
              </button>
              </div>
            )}
          </div>
        );
      })
  )}
</div>
</section>

) : paginaProfessor === "cursos" ? (
  <section className="professor-gerenciamento">
    <button
      type="button"
      className="professor-voltar"
      onClick={() => setPaginaProfessor("painel")}
    >
      ← Voltar ao painel
    </button>

    <div className="professor-gerenciamento-cabecalho">
      <h1>Cursos e Semestres</h1>
      <p>Gerencie os cursos e seus semestres.</p>
    </div>
    {cursoParaExcluir && (
  <div className="professor-confirmacao-exclusao">
    <h2>Confirmar exclusão de curso</h2>

    <p>
      Tem certeza de que deseja excluir o curso{" "}
      <strong>{cursoParaExcluir.nome}</strong>?
    </p>

    <p className="professor-confirmacao-aviso">
      Esta ação não poderá ser desfeita. Cursos com semestres,
      disciplinas ofertadas ou estudantes vinculados não poderão
      ser excluídos.
    </p>

    {erroExclusaoCurso && (
      <p className="admin-erro">{erroExclusaoCurso}</p>
    )}

    <div className="professor-confirmacao-acoes">
      <button
        type="button"
        className="professor-botao-secundario"
        onClick={() => {
          setCursoParaExcluir(null);
          setErroExclusaoCurso("");
        }}
        disabled={excluindoCurso}
      >
        Cancelar
      </button>

      <button
        type="button"
        className="professor-botao-confirmar-exclusao"
        onClick={excluirCursoProfessor}
        disabled={excluindoCurso}
      >
        {excluindoCurso ? "Excluindo..." : "Confirmar exclusão"}
      </button>
    </div>
  </div>
)}

{sucessoExclusaoCurso && (
  <p className="professor-cadastro-sucesso">
    {sucessoExclusaoCurso}
  </p>
)}

{semestreParaExcluir && (
  <div className="professor-confirmacao-exclusao">
    <h2>Confirmar exclusão de semestre</h2>

    <p>
      Tem certeza de que deseja excluir o{" "}
      <strong>{semestreParaExcluir.numero}º semestre</strong>?
    </p>

    <p className="professor-confirmacao-aviso">
      Esta ação não poderá ser desfeita. Semestres com disciplinas
      ofertadas ou estudantes vinculados não poderão ser excluídos.
    </p>

    {erroExclusaoSemestre && (
      <p className="admin-erro">{erroExclusaoSemestre}</p>
    )}

    <div className="professor-confirmacao-acoes">
      <button
        type="button"
        className="professor-botao-secundario"
        onClick={() => {
          setSemestreParaExcluir(null);
          setErroExclusaoSemestre("");
        }}
        disabled={excluindoSemestre}
      >
        Cancelar
      </button>

      <button
        type="button"
        className="professor-botao-confirmar-exclusao"
        onClick={excluirSemestreProfessor}
        disabled={excluindoSemestre}
      >
        {excluindoSemestre ? "Excluindo..." : "Confirmar exclusão"}
      </button>
    </div>
  </div>
)}

{sucessoExclusaoSemestre && (
  <p className="professor-cadastro-sucesso">
    {sucessoExclusaoSemestre}
  </p>
)}

    <div className="professor-gerenciamento-acoes">
      <input
        type="text"
        className="professor-gerenciamento-busca"
        placeholder="Buscar curso..."
        value={buscaCursosProfessor}
        onChange={(e) => setBuscaCursosProfessor(e.target.value)}
      />

      <div className="professor-gerenciamento-botoes">
        <button
          type="button"
          className="professor-botao-adicionar"
          onClick={() =>
            setFormularioCursoAberto(!formularioCursoAberto)
          }
        >
          {formularioCursoAberto ? "Cancelar" : "+ Adicionar Curso"}
        </button>

        <button
          type="button"
          className="professor-botao-secundario"
          onClick={() =>
            setFormularioSemestreAberto(!formularioSemestreAberto)
          }
        >
          {formularioSemestreAberto
            ? "Cancelar"
            : "+ Adicionar Semestre"}
        </button>
      </div>
    </div>


{formularioCursoAberto && (
  <div className="professor-formulario-disciplina">
    <h2>Adicionar novo curso</h2>
    <p>Preencha os dados para cadastrar um curso no sistema.</p>

    <form onSubmit={cadastrarCursoProfessor}>
      <div className="professor-formulario-campo">
        <label htmlFor="nome-novo-curso">Nome do curso</label>
        <input
          id="nome-novo-curso"
          type="text"
          placeholder="Ex.: Engenharia da Computação"
          value={nomeNovoCurso}
          onChange={(e) => setNomeNovoCurso(e.target.value)}
          maxLength={150}
          required
        />
      </div>

      <div className="professor-formulario-campo">
        <label htmlFor="descricao-novo-curso">Descrição</label>
        <textarea
          id="descricao-novo-curso"
          placeholder="Descreva brevemente o curso..."
          value={descricaoNovoCurso}
          onChange={(e) => setDescricaoNovoCurso(e.target.value)}
          maxLength={255}
          rows={3}
        />
      </div>

      {erroCadastroCurso && (
        <p className="admin-erro">{erroCadastroCurso}</p>
      )}

      {sucessoCadastroCurso && (
        <p className="professor-cadastro-sucesso">
          {sucessoCadastroCurso}
        </p>
      )}

      <button
        type="submit"
        className="professor-botao-adicionar"
        disabled={salvandoCurso}
      >
        {salvandoCurso ? "Salvando..." : "Cadastrar Curso"}
      </button>
    </form>
  </div>
)}


{formularioSemestreAberto && (
  <div className="professor-formulario-disciplina">
    <h2>Adicionar novo semestre</h2>
    <p>Cadastre um semestre vinculado a um curso existente.</p>

    <form onSubmit={cadastrarSemestreProfessor}>
      <div className="professor-formulario-campo">
        <label htmlFor="curso-novo-semestre">Curso</label>
        <select
          id="curso-novo-semestre"
          value={cursoNovoSemestre}
          onChange={(e) => setCursoNovoSemestre(e.target.value)}
          required
        >
          <option value="">Selecione um curso</option>
          {[...cursosProfessor]
            .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"))
            .map((curso) => (
              <option key={curso.id} value={curso.id}>
                {curso.nome}
              </option>
            ))}
        </select>
      </div>

      <div className="professor-formulario-campo">
        <label htmlFor="numero-novo-semestre">Número do semestre</label>
        <input
          id="numero-novo-semestre"
          type="number"
          min="1"
          max="20"
          step="1"
          placeholder="Ex.: 1"
          value={numeroNovoSemestre}
          onChange={(e) => setNumeroNovoSemestre(e.target.value)}
          required
        />
      </div>

      {erroCadastroSemestre && (
        <p className="admin-erro">{erroCadastroSemestre}</p>
      )}

      {sucessoCadastroSemestre && (
        <p className="professor-cadastro-sucesso">
          {sucessoCadastroSemestre}
        </p>
      )}

      <button
        type="submit"
        className="professor-botao-adicionar"
        disabled={salvandoSemestre}
      >
        {salvandoSemestre ? "Salvando..." : "Cadastrar Semestre"}
      </button>
    </form>
  </div>
)}

    <div className="professor-lista-disciplinas">
      {carregandoCursosProfessor ? (
        <p>Carregando cursos...</p>
      ) : erroCursosProfessor ? (
        <p className="admin-erro">{erroCursosProfessor}</p>
      ) : cursosProfessor.length === 0 ? (
        <p>Nenhum curso cadastrado.</p>
      ) : (
        cursosProfessor
          .filter((curso) =>
            curso.nome
              .toLowerCase()
              .includes(buscaCursosProfessor.trim().toLowerCase())
          )
          .sort((a, b) =>
            a.nome.localeCompare(b.nome, "pt-BR")
          )
          .map((curso) => {
            const semestresDoCurso = semestresProfessor.filter(
              (semestre) => semestre.cursoId === curso.id
            );

            const expandido =
              cursoExpandidoProfessor === curso.id;

            return (
              <div
                className="professor-disciplina-card"
                key={curso.id}
              >
                <div className="professor-disciplina-cabecalho">
                  <button
                    type="button"
                    className="professor-disciplina-expandir"
                    onClick={() =>
                      setCursoExpandidoProfessor(
                        expandido ? null : curso.id
                      )
                    }
                  >
                    <span>{expandido ? "⌄" : "›"}</span>
                    <strong>{curso.nome}</strong>
                  </button>

                  <span className="professor-disciplina-contagem">
                    {semestresDoCurso.length} semestres
                  </span>
                  <button
                  type="button"
                  className="professor-botao-remover"
                  onClick={() => {
                    setCursoParaExcluir(curso);
                    setErroExclusaoCurso("");
                    setSucessoExclusaoCurso("");
                  }}
                >
                  Remover Curso
                </button>
                </div>

                {expandido && (
                  <div className="professor-disciplina-assuntos">
                    {semestresDoCurso.length === 0 ? (
                      <p>
                        Nenhum semestre cadastrado neste curso.
                      </p>
                    ) : (
                      [...semestresDoCurso]
                        .sort((a, b) => a.numero - b.numero)
                        .map((semestre) => (
                          <div
                            className="professor-assunto-item"
                            key={semestre.id}
                          >
                            <span>
                              {semestre.numero}º semestre
                            </span>

                            <button
                              type="button"
                              className="professor-remover-assunto"
                              onClick={() => {
                                setSemestreParaExcluir(semestre);
                                setErroExclusaoSemestre("");
                                setSucessoExclusaoSemestre("");
                              }}
                            >
                              Remover
                            </button>
                          </div>
                        ))
                    )}

                    <button
                      type="button"
                      className="professor-adicionar-assunto-disciplina"
                      onClick={() => {
                        setFormularioSemestreAberto(true);
                        setCursoNovoSemestre(String(curso.id));
                        setNumeroNovoSemestre("");
                        setErroCadastroSemestre("");
                        setSucessoCadastroSemestre("");
                      }}
                    >
                      + Adicionar Semestre em {curso.nome}
                    </button>
                  </div>
                )}
              </div>
            );
          })
      )}
    </div>
  </section>

      ) : paginaProfessor === "avaliacoes" ? (
        <section className="professor-gerenciamento">
          <button
            type="button"
            className="professor-voltar"
            onClick={() => setPaginaProfessor("painel")}
          >
            ← Voltar ao painel
          </button>


<div className="professor-gerenciamento-cabecalho">
  <div className="professor-titulo-com-icone">
    <div className="professor-icone-pagina">
      <svg
        width="28"
        height="28"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <rect x="5" y="4" width="14" height="17" rx="2" />
        <rect x="9" y="2" width="6" height="4" rx="1" />
        <path d="M9 10h6" />
        <path d="M9 14h6" />
        <path d="M9 18h4" />
      </svg>
    </div>

    <div>
      <h1>Avaliações</h1>
      <p>Crie, edite e gerencie as avaliações do sistema.</p>
    </div>
  </div>
</div>

<div className="professor-acoes-gerenciamento">
  <div className="professor-campo-busca">
    <span className="professor-icone-busca">⌕</span>

  <input
    type="text"
    placeholder="Buscar avaliação, disciplina ou título..."
    value={buscaAvaliacoesProfessor}
    onChange={(evento) =>
      setBuscaAvaliacoesProfessor(evento.target.value)
    }
  />
  </div>


<button
  type="button"
  className="professor-botao-adicionar"
  onClick={() => {
    setFormularioAvaliacaoAberto(true);
    setTituloNovaAvaliacao("");
    setDisciplinaNovaAvaliacao("");
    setAssuntoNovaAvaliacao("");
    setErroCadastroAvaliacao("");
    setSucessoCadastroAvaliacao("");
  }}
>
  + Adicionar Avaliação
</button>

</div>


{formularioAvaliacaoAberto && (
  <div className="professor-formulario-avaliacao">
    <div className="professor-formulario-avaliacao-cabecalho">
      <h2>Adicionar Avaliação</h2>

      <button
        type="button"
        onClick={() => {
          setFormularioAvaliacaoAberto(false);
          setErroCadastroAvaliacao("");
        }}
        disabled={salvandoAvaliacao}
      >
        ×
      </button>
    </div>


    <form onSubmit={cadastrarAvaliacaoProfessor}>
      <div className="professor-formulario-avaliacao-campos">
        <div className="professor-campo-formulario">
          <label htmlFor="titulo-nova-avaliacao">
            Título da avaliação
          </label>

          <input
            id="titulo-nova-avaliacao"
            type="text"
            placeholder="Ex.: Prova 1 — Lógica de Programação"
            value={tituloNovaAvaliacao}
            onChange={(evento) =>
              setTituloNovaAvaliacao(evento.target.value)
            }
            maxLength={150}
            required
          />
        </div>

        <div className="professor-campo-formulario">
          <label htmlFor="disciplina-nova-avaliacao">
            Disciplina
          </label>


          <select
            id="disciplina-nova-avaliacao"
            value={disciplinaNovaAvaliacao}
            onChange={(evento) => {
              setDisciplinaNovaAvaliacao(evento.target.value);
              setAssuntoNovaAvaliacao("");
            }}
            required
          >
            <option value="">Selecione uma disciplina</option>

            {[...disciplinasProfessor]
              .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"))
              .map((disciplina) => (
                <option key={disciplina.id} value={disciplina.id}>
                  {disciplina.nome}
                </option>
              ))}
          </select>

        </div>

        <div className="professor-campo-formulario">
          <label htmlFor="assunto-nova-avaliacao">
            Assunto
          </label>


        <select
          id="assunto-nova-avaliacao"
          value={assuntoNovaAvaliacao}
          onChange={(evento) =>
            setAssuntoNovaAvaliacao(evento.target.value)
          }
          disabled={!disciplinaNovaAvaliacao}
          required
        >
          <option value="">Selecione um assunto</option>

          {assuntosProfessor
            .filter(
              (assunto) =>
                String(assunto.disciplina?.id) === String(disciplinaNovaAvaliacao)
            )
            .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"))
            .map((assunto) => (
              <option key={assunto.id} value={assunto.id}>
                {assunto.nome}
              </option>
            ))}
        </select>

        </div>
      </div>

      {erroCadastroAvaliacao && (
        <p className="admin-erro">{erroCadastroAvaliacao}</p>
      )}

      <div className="professor-formulario-avaliacao-acoes">
        <button
          type="button"
          className="professor-botao-secundario"
          onClick={() => {
            setFormularioAvaliacaoAberto(false);
            setErroCadastroAvaliacao("");
          }}
          disabled={salvandoAvaliacao}
        >
          Cancelar
        </button>

        <button
          type="submit"
          className="professor-botao-adicionar"
          disabled={salvandoAvaliacao}
        >
          {salvandoAvaliacao ? "Salvando..." : "Salvar Avaliação"}
        </button>
      </div>
    </form>
  </div>
)}

{avaliacaoEmEdicao && (
  <div className="professor-formulario-avaliacao">
    <div className="professor-formulario-avaliacao-cabecalho">
      <h2>Editar Avaliação</h2>
      <button
        type="button"
        onClick={() => {
          setAvaliacaoEmEdicao(null);
          setErroEdicaoAvaliacao("");
        }}
        disabled={salvandoEdicaoAvaliacao}
      >
        ×
      </button>
    </div>

      <form onSubmit={salvarEdicaoAvaliacao}>
      <div className="professor-formulario-avaliacao-campos">
        <div className="professor-campo-formulario">
          <label htmlFor="titulo-edicao-avaliacao">
            Título da avaliação
          </label>
          <input
            id="titulo-edicao-avaliacao"
            type="text"
            value={tituloEdicaoAvaliacao}
            onChange={(evento) =>
              setTituloEdicaoAvaliacao(evento.target.value)
            }
            maxLength={150}
            required
          />
        </div>

        <div className="professor-campo-formulario">
          <label htmlFor="disciplina-edicao-avaliacao">
            Disciplina
          </label>
          <select
            id="disciplina-edicao-avaliacao"
            value={disciplinaEdicaoAvaliacao}
            onChange={(evento) => {
              setDisciplinaEdicaoAvaliacao(evento.target.value);
              setAssuntoEdicaoAvaliacao("");
            }}
            disabled={avaliacaoEmEdicao.quantidadeQuestoes > 0}
            required
          >
            <option value="">Selecione uma disciplina</option>
            {[...disciplinasProfessor]
              .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"))
              .map((disciplina) => (
                <option key={disciplina.id} value={disciplina.id}>
                  {disciplina.nome}
                </option>
              ))}
          </select>
        </div>

        <div className="professor-campo-formulario">
          <label htmlFor="assunto-edicao-avaliacao">
            Assunto
          </label>
          <select
            id="assunto-edicao-avaliacao"
            value={assuntoEdicaoAvaliacao}
            onChange={(evento) =>
              setAssuntoEdicaoAvaliacao(evento.target.value)
            }
            disabled={
              !disciplinaEdicaoAvaliacao ||
              avaliacaoEmEdicao.quantidadeQuestoes > 0
            }
            required
          >
            <option value="">Selecione um assunto</option>
            {assuntosProfessor
              .filter(
                (assunto) =>
                  String(assunto.disciplina?.id) ===
                  String(disciplinaEdicaoAvaliacao)
              )
              .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"))
              .map((assunto) => (
                <option key={assunto.id} value={assunto.id}>
                  {assunto.nome}
                </option>
              ))}
          </select>
        </div>
      </div>


      {avaliacaoEmEdicao.quantidadeQuestoes > 0 && (
        <div className="professor-avaliacao-aviso">
          <span className="professor-avaliacao-aviso-icone">ⓘ</span>
          <div>
            <strong>Esta avaliação possui questões cadastradas.</strong>
            <p>
              Para preservar as questões existentes, apenas o título pode ser alterado.
            </p>
          </div>
        </div>
      )}


      {erroEdicaoAvaliacao && (
        <p className="admin-erro">{erroEdicaoAvaliacao}</p>
      )}

      <div className="professor-formulario-avaliacao-acoes">
        <button
          type="button"
          className="professor-botao-secundario"
          onClick={() => {
            setAvaliacaoEmEdicao(null);
            setErroEdicaoAvaliacao("");
          }}
          disabled={salvandoEdicaoAvaliacao}
        >
          Cancelar
        </button>

        <button
          type="submit"
          className="professor-botao-adicionar"
          disabled={salvandoEdicaoAvaliacao}
        >
          {salvandoEdicaoAvaliacao
            ? "Salvando..."
            : "Salvar Alterações"}
        </button>
      </div>
    </form>
  </div>
)}


{avaliacaoParaExcluir && (
  <div className="professor-confirmacao-exclusao">
    <div className="professor-confirmacao-exclusao-cabecalho">
      <div className="professor-confirmacao-exclusao-icone">
        !
      </div>
      <div>
        <h2>Excluir avaliação?</h2>
        <p>Confirme a exclusão da avaliação selecionada.</p>
      </div>
    </div>

    <div className="professor-confirmacao-exclusao-conteudo">
      <p>
        Você está prestes a excluir a avaliação:
      </p>

      <strong>{avaliacaoParaExcluir.titulo}</strong>

      <p>
        Esta ação não poderá ser desfeita. A exclusão será permitida
        somente se a avaliação não possuir questões ou resultados
        de estudantes vinculados.
      </p>
    </div>

    {erroExclusaoAvaliacao && (
      <p className="admin-erro">{erroExclusaoAvaliacao}</p>
    )}

    <div className="professor-confirmacao-exclusao-acoes">
      <button
        type="button"
        className="professor-botao-secundario"
        onClick={() => {
          setAvaliacaoParaExcluir(null);
          setErroExclusaoAvaliacao("");
        }}
        disabled={excluindoAvaliacao}
      >
        Cancelar
      </button>

      <button
        type="button"
        className="professor-botao-confirmar-exclusao"
        onClick={excluirAvaliacaoProfessor}
        disabled={excluindoAvaliacao}
      >
        {excluindoAvaliacao ? "Excluindo..." : "Excluir Avaliação"}
      </button>
    </div>
  </div>
)}

{avaliacaoGerenciandoQuestoes && (
  <section className="professor-gerenciamento-questoes">
    <div className="professor-gerenciamento-questoes-cabecalho">
      <div>
        <h2>Gerenciar Questões</h2>
        <p>{avaliacaoGerenciandoQuestoes.titulo}</p>
      </div>

      <button
        type="button"
        className="professor-botao-secundario"
        onClick={() => {
          setAvaliacaoGerenciandoQuestoes(null);
          setQuestoesProfessor([]);
          setErroQuestoesProfessor("");
        }}
      >
        Voltar às avaliações
      </button>
    </div>

    {carregandoQuestoesProfessor ? (
      <p>Carregando questões...</p>
    ) : erroQuestoesProfessor ? (
      <p className="admin-erro">{erroQuestoesProfessor}</p>
    ) : (
      <>

<div className="professor-gerenciamento-questoes-resumo">
  <div className="professor-gerenciamento-questoes-contador">
    <span>Questões cadastradas</span>
    <strong>{questoesProfessor.length}</strong>
  </div>

  <button
    type="button"
    className="professor-botao-adicionar"
    onClick={() => {
      setFormularioQuestaoAberto(true);
      setEnunciadoNovaQuestao("");
      setAlternativaANovaQuestao("");
      setAlternativaBNovaQuestao("");
      setAlternativaCNovaQuestao("");
      setAlternativaDNovaQuestao("");
      setRespostaCorretaNovaQuestao("");
      setErroCadastroQuestao("");
      setSucessoCadastroQuestao("");
    }}
    disabled={salvandoNovaQuestao}
  >
    + Adicionar Questão
  </button>
</div>

{formularioQuestaoAberto && (
  <div className="professor-formulario-questao">
    <div className="professor-formulario-questao-cabecalho">
      <div>
        <h3>Adicionar Nova Questão</h3>
        <p>Preencha os dados da questão e selecione a resposta correta.</p>
      </div>

      <button
        type="button"
        onClick={() => {
          setFormularioQuestaoAberto(false);
          setErroCadastroQuestao("");
        }}
        disabled={salvandoNovaQuestao}
        aria-label="Fechar formulário"
      >
        ×
      </button>
    </div>

    <form onSubmit={salvarNovaQuestao}>
      <div className="professor-campo-formulario">
        <label htmlFor="enunciado-nova-questao">
          Enunciado da questão
        </label>
        <textarea
          id="enunciado-nova-questao"
          value={enunciadoNovaQuestao}
          onChange={(evento) => setEnunciadoNovaQuestao(evento.target.value)}
          placeholder="Digite o enunciado da questão..."
          rows={4}
          required
        />
      </div>

      <div className="professor-formulario-questao-alternativas">
        {["A", "B", "C", "D"].map((letra) => {
          const valores = {
            A: alternativaANovaQuestao,
            B: alternativaBNovaQuestao,
            C: alternativaCNovaQuestao,
            D: alternativaDNovaQuestao
          };

          const atualizadores = {
            A: setAlternativaANovaQuestao,
            B: setAlternativaBNovaQuestao,
            C: setAlternativaCNovaQuestao,
            D: setAlternativaDNovaQuestao
          };

          return (
            <div className="professor-campo-formulario" key={letra}>
              <label htmlFor={`alternativa-nova-questao-${letra}`}>
                Alternativa {letra}
              </label>
              <input
                id={`alternativa-nova-questao-${letra}`}
                type="text"
                value={valores[letra]}
                onChange={(evento) => atualizadores[letra](evento.target.value)}
                placeholder={`Digite a alternativa ${letra}`}
                maxLength={255}
                required
              />
            </div>
          );
        })}
      </div>

      <div className="professor-campo-formulario">
        <label htmlFor="resposta-correta-nova-questao">
          Resposta correta
        </label>
        <select
          id="resposta-correta-nova-questao"
          value={respostaCorretaNovaQuestao}
          onChange={(evento) => setRespostaCorretaNovaQuestao(evento.target.value)}
          required
        >
          <option value="">Selecione a alternativa correta</option>
          <option value="A">Alternativa A</option>
          <option value="B">Alternativa B</option>
          <option value="C">Alternativa C</option>
          <option value="D">Alternativa D</option>
        </select>
      </div>

      {erroCadastroQuestao && (
        <p className="admin-erro">{erroCadastroQuestao}</p>
      )}

      <div className="professor-formulario-questao-acoes">
        <button
          type="button"
          className="professor-botao-secundario"
          onClick={() => {
            setFormularioQuestaoAberto(false);
            setErroCadastroQuestao("");
          }}
          disabled={salvandoNovaQuestao}
        >
          Cancelar
        </button>

        <button
          type="submit"
          className="professor-botao-adicionar"
          disabled={salvandoNovaQuestao}
        >
          {salvandoNovaQuestao ? "Salvando..." : "Salvar Questão"}
        </button>
      </div>
    </form>
  </div>
)}

{questaoEmEdicao && (
  <div className="professor-formulario-questao">
    <div className="professor-formulario-questao-cabecalho">
      <div>
        <h3>Editar Questão</h3>
        <p>Altere os dados da questão selecionada.</p>
      </div>

      <button
        type="button"
        onClick={() => {
          setQuestaoEmEdicao(null);
          setErroEdicaoQuestao("");
        }}
        disabled={salvandoEdicaoQuestao}
        aria-label="Fechar edição"
      >
        ×
      </button>
    </div>

    <form onSubmit={salvarEdicaoQuestao}>
      <div className="professor-campo-formulario">
        <label htmlFor="editar-enunciado-questao">
          Enunciado da questão
        </label>

        <textarea
          id="editar-enunciado-questao"
          value={questaoEmEdicao.enunciado ?? ""}
          onChange={(evento) =>
            setQuestaoEmEdicao({
              ...questaoEmEdicao,
              enunciado: evento.target.value
            })
          }
          rows={4}
          required
        />
      </div>

      <div className="professor-formulario-questao-alternativas">
        {["A", "B", "C", "D"].map((letra) => (
          <div className="professor-campo-formulario" key={letra}>
            <label htmlFor={`editar-alternativa-${letra}`}>
              Alternativa {letra}
            </label>

            <input
              id={`editar-alternativa-${letra}`}
              type="text"
              value={questaoEmEdicao[`alternativa${letra}`] ?? ""}
              onChange={(evento) =>
                setQuestaoEmEdicao({
                  ...questaoEmEdicao,
                  [`alternativa${letra}`]: evento.target.value
                })
              }
              maxLength={255}
              required
            />
          </div>
        ))}
      </div>

      <div className="professor-campo-formulario">
        <label htmlFor="editar-resposta-correta">
          Resposta correta
        </label>

        <select
          id="editar-resposta-correta"
          value={questaoEmEdicao.respostaCorreta ?? ""}
          onChange={(evento) =>
            setQuestaoEmEdicao({
              ...questaoEmEdicao,
              respostaCorreta: evento.target.value
            })
          }
          required
        >
          <option value="">Selecione a alternativa correta</option>
          <option value="A">Alternativa A</option>
          <option value="B">Alternativa B</option>
          <option value="C">Alternativa C</option>
          <option value="D">Alternativa D</option>
        </select>
      </div>

      {erroEdicaoQuestao && (
        <p className="admin-erro">{erroEdicaoQuestao}</p>
      )}

      <div className="professor-formulario-questao-acoes">
        <button
          type="button"
          className="professor-botao-secundario"
          onClick={() => {
            setQuestaoEmEdicao(null);
            setErroEdicaoQuestao("");
          }}
          disabled={salvandoEdicaoQuestao}
        >
          Cancelar
        </button>

        <button
          type="submit"
          className="professor-botao-adicionar"
          disabled={salvandoEdicaoQuestao}
        >
          {salvandoEdicaoQuestao ? "Salvando..." : "Salvar Alterações"}
        </button>
      </div>
    </form>
  </div>
)}

{sucessoEdicaoQuestao && (
  <p className="professor-cadastro-sucesso">
    {sucessoEdicaoQuestao}
  </p>
)}


{questaoParaExcluir && (
  <div className="professor-confirmacao-exclusao-questao">
    <div className="professor-confirmacao-exclusao-questao-cabecalho">
      <h3>Excluir Questão</h3>
      <button
        type="button"
        onClick={() => {
          setQuestaoParaExcluir(null);
          setErroExclusaoQuestao("");
        }}
        disabled={excluindoQuestao}
        aria-label="Fechar confirmação"
      >
        ×
      </button>
    </div>

    <p>
      Tem certeza de que deseja excluir esta questão?
    </p>

    <div className="professor-confirmacao-exclusao-questao-enunciado">
      {questaoParaExcluir.enunciado}
    </div>

    <p className="professor-confirmacao-exclusao-questao-aviso">
      Esta ação não poderá ser desfeita. Questões de avaliações que já
      possuem resultados de estudantes não poderão ser excluídas.
    </p>

    {erroExclusaoQuestao && (
      <p className="admin-erro">{erroExclusaoQuestao}</p>
    )}

    <div className="professor-confirmacao-exclusao-questao-acoes">
      <button
        type="button"
        className="professor-botao-secundario"
        onClick={() => {
          setQuestaoParaExcluir(null);
          setErroExclusaoQuestao("");
        }}
        disabled={excluindoQuestao}
      >
        Cancelar
      </button>

<button
  type="button"
  className="professor-confirmar-exclusao-questao"
  onClick={excluirQuestaoProfessor}
  disabled={excluindoQuestao}
>
  {excluindoQuestao ? "Excluindo..." : "Confirmar Exclusão"}
</button>

    </div>
  </div>
)}

{sucessoExclusaoQuestao && (
  <p className="professor-cadastro-sucesso">
    {sucessoExclusaoQuestao}
  </p>
)}

{sucessoCadastroQuestao && (
  <p className="professor-cadastro-sucesso">
    {sucessoCadastroQuestao}
  </p>
)}

        {questoesProfessor.length === 0 ? (
          <p className="professor-gerenciamento-questoes-lista-vazia">
  Esta avaliação ainda não possui questões cadastradas.
</p>
        ) : (
          <div className="professor-gerenciamento-questoes-lista">
            {questoesProfessor.map((questao, indice) => (
              <article
                key={questao.id}
                className="professor-gerenciamento-questao-card"
              >

<div className="professor-gerenciamento-questao-topo">
  <span>Questão {indice + 1}</span>

  <div className="professor-gerenciamento-questao-acoes">
    <span>Resposta correta: {questao.respostaCorreta}</span>

    <button
      type="button"
      className="professor-botao-editar-questao"
      onClick={() => {
        setQuestaoEmEdicao(questao);
        setFormularioQuestaoAberto(false);
        setErroEdicaoQuestao("");
        setSucessoEdicaoQuestao("");
        setSucessoCadastroQuestao("");
      }}
    >
      <icon name="pencil" inline/> Editar
    </button>

<button
  type="button"
  className="professor-botao-excluir-questao"
  onClick={() => {
    setQuestaoParaExcluir(questao);
    setQuestaoEmEdicao(null);
    setFormularioQuestaoAberto(false);
    setErroExclusaoQuestao("");
    setSucessoExclusaoQuestao("");
  }}
  disabled={excluindoQuestao}
>
  ✕ Excluir
</button>

  </div>
</div>


                <h3>{questao.enunciado}</h3>

                <div className="professor-gerenciamento-alternativas">
                  {["A", "B", "C", "D"].map((letra) => (
                    <div
                      key={letra}
                      className={
                        questao.respostaCorreta === letra
                          ? "professor-gerenciamento-alternativa correta"
                          : "professor-gerenciamento-alternativa"
                      }
                    >
                      <strong>{letra}</strong>
                      <span>{questao[`alternativa${letra}`]}</span>
                    </div>
                  ))}
                </div>
              </article>
            ))}
          </div>
        )}
      </>
    )}
  </section>
)}

{sucessoCadastroAvaliacao && (
  <p className="professor-cadastro-sucesso">
    {sucessoCadastroAvaliacao}
  </p>
)}

<div className="professor-tabela-avaliacoes">
  <table>
    <thead>
      <tr>
        <th>Título da Avaliação</th>
        <th>Disciplina</th>
        <th>Questões</th>
        <th>Data de Criação</th>
        <th>Ações</th>
      </tr>
    </thead>


<tbody>
  {carregandoAvaliacoesProfessor ? (
    <tr>
      <td colSpan="5">Carregando avaliações...</td>
    </tr>
  ) : erroAvaliacoesProfessor ? (
    <tr>
      <td colSpan="5" className="admin-erro">
        {erroAvaliacoesProfessor}
      </td>
    </tr>
  ) : avaliacoesProfessor.filter((avaliacao) => {
      const busca = buscaAvaliacoesProfessor.trim().toLowerCase();

      return (
        avaliacao.titulo.toLowerCase().includes(busca) ||
        avaliacao.disciplinaNome.toLowerCase().includes(busca)
      );
    }).length === 0 ? (
    <tr>
      <td colSpan="5">Nenhuma avaliação encontrada.</td>
    </tr>
  ) : (
    avaliacoesProfessor
      .filter((avaliacao) => {
        const busca = buscaAvaliacoesProfessor.trim().toLowerCase();

        return (
          avaliacao.titulo.toLowerCase().includes(busca) ||
          avaliacao.disciplinaNome.toLowerCase().includes(busca)
        );
      })
      .sort((a, b) => a.titulo.localeCompare(b.titulo, "pt-BR"))
      .map((avaliacao) => (
        <tr key={avaliacao.id}>
          <td>{avaliacao.titulo}</td>
          <td>{avaliacao.disciplinaNome}</td>
          <td>{avaliacao.quantidadeQuestoes}</td>
<td>
  {avaliacao.dataCriacao
    ? avaliacao.dataCriacao
        .split("T")[0]
        .split("-")
        .reverse()
        .join("/")
    : "Não registrada"}
</td>

          <td>
            <div className="professor-acoes-avaliacao">
            <button
              type="button"
              className="professor-gerenciar-questoes"
              title="Gerenciar questões"
              aria-label={`Gerenciar questões de ${avaliacao.titulo}`}
              onClick={() => carregarQuestoesProfessor(avaliacao)}
            >
              ☷
            </button>

          <button
            type="button"
            className="professor-editar-avaliacao"
            title="Editar avaliação"
            onClick={() => abrirEdicaoAvaliacao(avaliacao)}
          >
            ✎
          </button>

          <button
            type="button"
            className="professor-excluir-avaliacao"
            title="Excluir avaliação"
            onClick={() => abrirExclusaoAvaliacao(avaliacao)}
          >
            🗑
          </button>

            </div>
          </td>
        </tr>
      ))
  )}
</tbody>

  </table>
</div>

        </section>
      ) : null}
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

            <div className="resultado-acoes">
  <button
    className="botao-voltar-avaliacoes"
    type="button"
    onClick={() => {
      setResultado(null);
      setAvaliacaoSelecionada(null);
      setAvaliacaoId(null);
      setQuestoes([]);
      setRespostas({});
      setErro("");
      setPaginaAtiva("avaliacoes");
    }}
  >
    ← Voltar às avaliações
  </button>

  <button
  className="botao-principal botao-refazer"
  type="button"
  onClick={() => {
    setResultado(null);
    setRespostas({});
    setErro("");
    setPaginaAtiva("avaliacoes");
  }}
>
  Refazer avaliação
</button>
</div>
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
              <nav className="menu-lateral" aria-label="Navegação do estudante">
  <div className="menu-lateral-titulo">MENU PRINCIPAL</div>

  <button
    type="button"
    className={paginaAtiva === "dashboard" ? "menu-item ativo" : "menu-item"}
    onClick={() => setPaginaAtiva("dashboard")}
  >
    Dashboard
  </button>
  <button
  type="button"
  className={paginaAtiva === "disciplinas" ? "menu-item ativo" : "menu-item"}
  onClick={() => setPaginaAtiva("disciplinas")}
>
  Minhas Disciplinas
</button>

  <button
    type="button"
    className={paginaAtiva === "avaliacoes" ? "menu-item ativo" : "menu-item"}
    onClick={() => setPaginaAtiva("avaliacoes")}
  >
    Avaliações
  </button>


  <button
    type="button"
    className={paginaAtiva === "desempenho" ? "menu-item ativo" : "menu-item"}
    onClick={() => setPaginaAtiva("desempenho")}
  >
    Desempenho
  </button>

  <button
    type="button"
    className={paginaAtiva === "evolucao" ? "menu-item ativo" : "menu-item"}
    onClick={() => setPaginaAtiva("evolucao")}
  >
    Evolução
  </button>


<button
  type="button"
  className={paginaAtiva === "plano-estudos" ? "menu-item ativo" : "menu-item"}
  onClick={() => setPaginaAtiva("plano-estudos")}
>
  Plano de Estudos
</button>

  <button
    type="button"
    className={paginaAtiva === "materiais" ? "menu-item ativo" : "menu-item"}
    onClick={() => setPaginaAtiva("materiais")}
  >
    Materiais de estudo
  </button>
  <button
  type="button"
  className={paginaAtiva === "central-estudante" ? "menu-item ativo" : "menu-item"}
  onClick={() => {
    setAbaCentral("notificacoes");
    setPaginaAtiva("central-estudante");
  }}
>

<span className="notificacoes-menu-conteudo">
  <span>Notificações</span>
  {quantidadeNaoLidas > 0 && (
    <span className="notificacoes-contador">
      {quantidadeNaoLidas > 99 ? "99+" : quantidadeNaoLidas}
    </span>
  )}
</span>

</button>

</nav>

<div className="conteudo-principal">
  {paginaAtiva === "disciplinas" && (
    <>
  <section className="dashboard-academico">
    <div className="dashboard-cabecalho">
      <div>
        <span className="secao-etiqueta">Área do estudante</span>
        <h1>Minhas disciplinas</h1>
        <p>Disciplinas vinculadas ao seu curso e semestre.</p>
      </div>
    </div>

    {disciplinasMatriculadas.length === 0 ? (
  <p>Nenhuma disciplina matriculada.</p>
) : (
  <div className="disciplinas-grid">
    {disciplinasMatriculadas.map((disciplina) => (
  <div className="disciplina-card" key={disciplina.id}>
    <h3>{disciplina.disciplinaNome}</h3>

    <button
      type="button"
      className="botao-ver-assuntos"
      onClick={() => carregarAssuntos(disciplina.disciplinaId)}
    >
      {disciplinaAssuntosAberta === disciplina.disciplinaId
        ? "Ocultar assuntos"
        : "Ver assuntos"}
    </button>

    {disciplinaAssuntosAberta === disciplina.disciplinaId && (
      <div className="lista-assuntos">
        {carregandoAssuntos === disciplina.disciplinaId ? (
          <p>Carregando assuntos...</p>
        ) : !assuntosPorDisciplina[disciplina.disciplinaId] ? (
          <p>Não foi possível carregar os assuntos.</p>
        ) : assuntosPorDisciplina[disciplina.disciplinaId].length === 0 ? (
          <p>Nenhum assunto cadastrado para esta disciplina.</p>
        ) : (
          assuntosPorDisciplina[disciplina.disciplinaId].map((assunto) => (
            <div className="assunto-item" key={assunto.id}>
  <h4>{assunto.nome}</h4>
</div>
          ))
        )}
      </div>
    )}
  </div>
))}
  </div>
)}
    </section>

  <section className="dashboard-academico">
    <div className="dashboard-cabecalho">
      <div>
        <span className="secao-etiqueta">Configuração acadêmica</span>
        <h2>Gerenciar matrícula</h2>
        <p>Escolha seu curso, semestre e as disciplinas que deseja cursar.</p>
      </div>
    </div>
    <div className="gerenciar-matricula-formulario">
  <label htmlFor="curso-matricula">Curso</label>

  <select
    id="curso-matricula"
    value={cursoSelecionado}
    onChange={(e) => setCursoSelecionado(e.target.value)}
  >
    <option value="">Selecione um curso</option>

    {cursos.map((curso) => (
      <option key={curso.id} value={curso.id}>
        {curso.nome}
      </option>
    ))}
  </select>
  <label htmlFor="semestre-matricula">Semestre</label>

<select
  id="semestre-matricula"
  value={semestreSelecionado}
  onChange={(e) => setSemestreSelecionado(e.target.value)}
>
  <option value="">Selecione um semestre</option>

  {Array.from({ length: 10 }, (_, index) => (
    <option key={index + 1} value={index + 1}>
      {index + 1}º semestre
    </option>
  ))}
</select>
<label>Disciplinas disponíveis para o semestre</label>

<div className="lista-disciplinas-matricula">
  {ofertasDisciplinas.length === 0 ? (
    <p>Nenhuma disciplina disponível para o curso e semestre selecionados.</p>
  ) : (
    ofertasDisciplinas.map((disciplina) => (
      <label key={disciplina.id} className="opcao-disciplina-matricula">
        <input
          type="checkbox"
          checked={disciplinasSelecionadas.some(
            (id) => Number(id) === Number(disciplina.id)
          )}
          onChange={(e) => {
            setDisciplinasSelecionadas((anteriores) =>
              e.target.checked
                ? [...anteriores, disciplina.id]
                : anteriores.filter(
                    (id) => Number(id) !== Number(disciplina.id)
                  )
            );
          }}
        />
        <span>{disciplina.disciplinaNome}</span>
      </label>
    ))
  )}
</div>

<button
  type="button"
  className="botao-salvar-matricula"
  onClick={salvarConfiguracaoAcademica}
  disabled={salvandoConfiguracao || disciplinasSelecionadas.length === 0}
>
  {salvandoConfiguracao ? "Salvando..." : "Salvar alterações"}
</button>

</div>
</section>
  </>
)}
  {paginaAtiva === "dashboard" && (
  <section className="dashboard-academico" id="dashboard">
    <div className="dashboard-cabecalho">
      <div>
        <span className="secao-etiqueta">Sua área de aprendizagem</span>
        <h1>Bem-vindo de volta, {usuario?.nome?.trim().split(/\s+/)[0] || "estudante"}!</h1>
        <p>
          Cada passo conta na sua jornada de aprendizagem.
          Vamos continuar evoluindo?
        </p>
      </div>
    </div>

    <div className="home-recomendacao">
  <div className="home-recomendacao-conteudo">
    <span className="home-etiqueta">RECOMENDADO PARA VOCÊ</span>

    {assuntoRecomendado ? (
      <>
        <h2>
          Reforce seus conhecimentos em{" "}
          {assuntoRecomendado.assuntoNome || "um assunto importante"}
        </h2>

        <p>
          {assuntoRecomendado.prioridade === "ALTA"
            ? "Identificamos que este assunto merece atenção especial. Priorize uma revisão antes da próxima avaliação."
            : assuntoRecomendado.prioridade === "MEDIA"
            ? "Você pode fortalecer seus conhecimentos neste assunto com uma revisão direcionada."
            : "Continue praticando para consolidar seus conhecimentos e manter seu progresso."}
        </p>

        <span className="home-recomendacao-prioridade">
          Prioridade:{" "}
          {assuntoRecomendado.prioridade === "ALTA"
            ? "Alta"
            : assuntoRecomendado.prioridade === "MEDIA"
            ? "Média"
            : assuntoRecomendado.prioridade === "BAIXA"
            ? "Baixa"
            : "Não definida"}
        </span>

        <button
          type="button"
          className="home-botao-principal"
          onClick={() => setPaginaAtiva("materiais")}
        >
          Começar revisão →
        </button>
      </>
    ) : (
      <>
        <h2>Seu próximo passo começa aqui</h2>

        <p>
          Realize sua primeira avaliação para que o SAP identifique
          seus pontos de atenção e recomende conteúdos de estudo
          personalizados.
        </p>

        <button
          type="button"
          className="home-botao-principal"
          onClick={() => setPaginaAtiva("avaliacoes")}
        >
          Realizar avaliação →
        </button>
      </>
    )}
  </div>
</div>

    <div className="home-acoes">
      <div className="home-secao-cabecalho">
        <h2>O que vamos fazer hoje?</h2>
        <p>Escolha como deseja continuar sua aprendizagem.</p>
      </div>

      <div className="home-acoes-grid">
        <button
          type="button"
          className="home-acao-card"
          onClick={() => setPaginaAtiva("avaliacoes")}
        >
          <span className="home-acao-icone">✓</span>
          <strong>Realizar avaliação</strong>
          <span>
            Teste seus conhecimentos e identifique
            oportunidades de aprendizagem.
          </span>
        </button>


        <button
         type="button"
          className="home-acao-card"
         onClick={() => setPaginaAtiva("plano-estudos")}
        >
      <span className="home-acao-icone">▦</span>
      <strong>Meu plano de estudos</strong>
      <span>
        Organize seus estudos, acompanhe suas metas
        e revise os assuntos que precisam de atenção.
      </span>
      </button>


        <button
          type="button"
          className="home-acao-card"
          onClick={() => setPaginaAtiva("materiais")}
        >
          <span className="home-acao-icone">▤</span>
          <strong>Biblioteca de estudos</strong>
          <span>
            Encontre livros e materiais relacionados
            aos assuntos que você está estudando.
          </span>
        </button>


        <button
           type="button"
          className="home-acao-card"
          onClick={() => setPaginaAtiva("evolucao")}
        >
        <span className="home-acao-icone">↗</span>
        <strong>Minha evolução</strong>
        <span>
            Acompanhe seu progresso acadêmico e compare
           seu desempenho ao longo das avaliações.
        </span>
      </button>

      </div>
    </div>

    <div className="home-dica">
      <span className="home-dica-icone">✦</span>

      <div>
        <h3>Dica para seus estudos</h3>
        <p>
          Reserve alguns minutos para revisar os assuntos
          em que teve mais dificuldade antes de iniciar
          uma nova avaliação.
        </p>
      </div>
    </div>
  </section>
)}


{paginaAtiva === "central-estudante" && (
  <section className="central-estudante">
    <div className="central-cabecalho">
      <span className="central-etiqueta">
        PERSONALIZAÇÃO E COMUNICAÇÃO
      </span>
      <h1>Central do Estudante</h1>
      <p>
        Acompanhe seus avisos e personalize sua experiência acadêmica.
      </p>
    </div>

    <div className="central-abas">
      <button
        type="button"
        className={abaCentral === "notificacoes" ? "central-aba ativa" : "central-aba"}
        onClick={() => setAbaCentral("notificacoes")}
      >
        Notificações
      </button>

      <button
        type="button"
        className={abaCentral === "preferencias" ? "central-aba ativa" : "central-aba"}
        onClick={() => setAbaCentral("preferencias")}
      >
        Preferências
      </button>
    </div>


{abaCentral === "notificacoes" && (
  <div className="central-conteudo">
    <h2>Notificações</h2>

    <div className="central-secao">
      <h3>Avisos acadêmicos</h3>

      <p>
        Acompanhe suas avaliações, recomendações de estudo
        e comunicados acadêmicos.
      </p>


{quantidadeNaoLidas === 0
  ? "Você não possui notificações não lidas."
  : `Você possui ${quantidadeNaoLidas} ${
      quantidadeNaoLidas === 1
        ? "notificação não lida"
        : "notificações não lidas"
    }.`}

    </div>

    <div className="central-secao">
      <h3>Histórico de notificações</h3>

      {carregandoNotificacoes && (
        <p>Carregando notificações...</p>
      )}

      {erroNotificacoes && (
        <p className="central-erro">
          {erroNotificacoes}
        </p>
      )}

      {!carregandoNotificacoes &&
        !erroNotificacoes &&
        notificacoes.length === 0 && (
          <p>
            Você ainda não possui notificações.
            Quando houver novidades acadêmicas,
            elas aparecerão aqui.
          </p>
        )}

      {!carregandoNotificacoes &&
        !erroNotificacoes &&
        notificacoes.length > 0 && (
          <div className="central-lista-notificacoes">
            {notificacoes.map((notificacao) => (
              <div
                key={notificacao.id}
                className={
                  notificacao.lida
                    ? "central-notificacao"
                    : "central-notificacao nao-lida"
                }
              >
                <div className="central-notificacao-topo">
                  <span className="central-notificacao-categoria">

                {{
                PLANO_ESTUDOS: "Plano de Estudos",
                META_PENDENTE: "Meta pendente",
                RECOMENDACAO_REVISAO: "Recomendação de revisão",
                AVALIACAO_PROXIMA: "Avaliação próxima",
                EVOLUCAO_DESEMPENHO: "Evolução de desempenho",
                RESUMO_SEMANAL: "Resumo semanal"
              }[notificacao.categoria] || notificacao.categoria}

                  </span>

                  {!notificacao.lida && (
                    <span className="central-notificacao-status">
                      Não lida
                    </span>
                  )}
                </div>

                <h4>{notificacao.titulo}</h4>

                <p>{notificacao.mensagem}</p>

                <div className="central-notificacao-rodape">
                  <span>
                    {notificacao.dataCriacao
                      ? new Date(
                          notificacao.dataCriacao
                        ).toLocaleString("pt-BR")
                      : ""}
                  </span>

                  {!notificacao.lida && (
                    <button
                      type="button"
                      className="central-botao-ler"
                      onClick={() =>
                        marcarNotificacaoComoLida(notificacao.id)
                      }
                    >
                      Marcar como lida
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
    </div>
  </div>
)}




{abaCentral === "preferencias" && (
  <div className="central-conteudo">
    <div className="preferencias-cabecalho">
      <div>
        <h2>Preferências de notificações</h2>
        <p>Personalize os avisos que deseja receber do SAP.</p>
      </div>
      <span className="preferencias-icone">⚙</span>
    </div>

    <div className="preferencias-lista">
      {[
        {
          chave: "notificacoesAtivas",
          titulo: "Ativar notificações",
          descricao: "Receber avisos dentro do SAP"
        },
        {
          chave: "lembretesPlano",
          titulo: "Lembretes do plano de estudos",
          descricao: "Avisar sobre metas e revisões programadas"
        },
        {
          chave: "metasPendentes",
          titulo: "Metas pendentes",
          descricao: "Avisar sobre metas próximas do prazo"
        },
        {
          chave: "recomendacoesRevisao",
          titulo: "Recomendações de revisão",
          descricao: "Sugerir revisões com base no desempenho"
        },
        {
          chave: "avaliacoesProximas",
          titulo: "Avaliações próximas",
          descricao: "Lembrar datas de avaliações cadastradas"
        },
        {
          chave: "evolucaoDesempenho",
          titulo: "Evolução de desempenho",
          descricao: "Destacar melhorias no aprendizado"
        },
        {
          chave: "resumoSemanal",
          titulo: "Resumo semanal",
          descricao: "Receber um balanço das metas da semana"
        }
      ].map((opcao) => (
        <label
          key={opcao.chave}
          className={
            "preferencias-opcao" +
            (!preferenciasNotificacoes.notificacoesAtivas &&
            opcao.chave !== "notificacoesAtivas"
              ? " desabilitada"
              : "")
          }
        >
          <div className="preferencias-opcao-texto">
            <strong>{opcao.titulo}</strong>
            <span>{opcao.descricao}</span>
          </div>

          <input
            type="checkbox"
            className="preferencias-checkbox"
            checked={preferenciasNotificacoes[opcao.chave]}
            disabled={
              opcao.chave !== "notificacoesAtivas" &&
              !preferenciasNotificacoes.notificacoesAtivas
            }
            onChange={(evento) => {
              const novasPreferencias = {
                ...preferenciasNotificacoes,
                [opcao.chave]: evento.target.checked
              };

              salvarPreferenciasNotificacoes(novasPreferencias);
            }}
          />
        </label>
      ))}
    </div>

    <div className="preferencias-antecedencia">
      <h3>Antecedência dos lembretes de estudo</h3>

      {[
        {
          valor: 0,
          texto: "No próprio dia"
        },
        {
          valor: 1,
          texto: "Um dia antes e no próprio dia"
        },
        {
          valor: 2,
          texto: "Dois dias antes e no próprio dia"
        }
      ].map((opcao) => (
        <label
          key={opcao.valor}
          className="preferencias-radio-opcao"
        >
          <input
            type="radio"
            name="antecedenciaLembretes"
            value={opcao.valor}
            checked={
              preferenciasNotificacoes.antecedenciaLembretes === opcao.valor
            }
            disabled={
              !preferenciasNotificacoes.notificacoesAtivas ||
              !preferenciasNotificacoes.lembretesPlano
            }
            onChange={() => {
              const novasPreferencias = {
                ...preferenciasNotificacoes,
                antecedenciaLembretes: opcao.valor
              };

              salvarPreferenciasNotificacoes(novasPreferencias);
            }}
          />
          <span>{opcao.texto}</span>
        </label>
      ))}
    </div>
  </div>
)}
  </section>
)}

{paginaAtiva === "plano-estudos" && (
  <section className="plano-estudos">
    <div className="plano-cabecalho">
      <span className="plano-etiqueta">
        ORGANIZAÇÃO ACADÊMICA
      </span>
      <h1>Meu Plano de Estudos</h1>
      <p>
        Organize sua rotina, estabeleça metas e foque nos assuntos
        que mais precisam de atenção.
      </p>
    </div>

    {carregandoMetas ? (
      <p>Carregando seu plano de estudos...</p>
    ) : erroMetas ? (
      <p className="materiais-erro">{erroMetas}</p>
        ) : (
      <>
        <div className="plano-indicadores">
        <article className="plano-indicador">
          <div className="plano-indicador-icone">◎</div>
          <div>
            <span>Metas do período</span>
            <strong>{totalMetas}</strong>
          </div>
        </article>

        <article className="plano-indicador">
          <div className="plano-indicador-icone">✓</div>
          <div>
            <span>Metas concluídas</span>
            <strong>{metasConcluidas}</strong>
          </div>
        </article>

        <article className="plano-indicador">
          <div className="plano-indicador-icone">▥</div>
          <div className="plano-progresso-conteudo">
            <span>Progresso geral</span>
            <strong>{progressoMetas}%</strong>
            <div className="plano-progresso-barra">
              <div
                className="plano-progresso-preenchimento"
                style={{ width: `${progressoMetas}%` }}
              />
            </div>
          </div>
        </article>
      </div>


<div className="plano-paineis">
  <section className="plano-semana">
    <div className="plano-painel-cabecalho">
      <div>
        <h2>Minha semana</h2>
        <p>Visualize seus dias de estudo e metas da semana.</p>
      </div>

      <span className="plano-semana-contador">
        {metasSemanaPlano} {metasSemanaPlano === 1 ? "meta" : "metas"} na semana
      </span>
    </div>

    <div className="plano-semana-dias">
      {diasSemanaPlano.map((dia) => (
        <div
          key={dia.data}
          className={`plano-semana-dia ${dia.possuiMetas ? "com-metas" : ""} ${dia.ehHoje ? "dia-atual" : ""}`}
          title={`${dia.metas.length} ${dia.metas.length === 1 ? "meta prevista" : "metas previstas"}`}
        >
          <span>{dia.dia}</span>
          <strong>{dia.numero}</strong>
          {dia.possuiMetas && <span className="plano-dia-marcador" />}
        </div>
      ))}
    </div>
  </section>

  <section className="plano-metas">
    <div className="plano-painel-cabecalho">
      <div>
        <h2>Minhas metas</h2>
        <p>Objetivos personalizados gerados pelo SAP.</p>
      </div>
      <span className="plano-metas-icone">◎</span>
    </div>

    {metasEstudo.length === 0 ? (
      <div className="plano-metas-vazio">
        <p>
  Nenhuma meta pendente no momento. Se você já concluiu
  suas atividades, consulte as metas realizadas acima
  ou faça uma nova avaliação para atualizar seu plano de estudos.
</p>
        <button
          type="button"
          onClick={() => setPaginaAtiva("avaliacoes")}
        >
          Realizar avaliação
        </button>
      </div>
    ) : (
      <div className="plano-metas-lista">
        {metasEstudo.map((meta) => (
          <label
            key={meta.id}
            className={`plano-meta-item ${meta.concluida ? "concluida" : ""}`}
          >
            <input
              type="checkbox"
              checked={meta.concluida === true}
              onChange={(evento) =>
                atualizarConclusaoMeta(meta.id, evento.target.checked)
              }
            />

            <div className="plano-meta-conteudo">
              <strong>{meta.titulo}</strong>
              <span>
                Até{" "}
                {meta.dataPrevista
                  ? new Date(`${meta.dataPrevista}T12:00:00`).toLocaleDateString(
                      "pt-BR",
                      { day: "2-digit", month: "2-digit" }
                    )
                  : "sem prazo definido"}
              </span>
            </div>
          </label>
        ))}
      </div>
    )}
  </section>
</div>

<section className="plano-foco">
  <div className="plano-foco-cabecalho">
    <div>
      <h2>Foco de estudo (recomendações)</h2>
      <p>
        Assuntos sugeridos com base no seu desempenho nas avaliações.
      </p>
    </div>
  </div>

  {desempenhosMaisRecentes.length === 0 ? (
    <div className="plano-foco-vazio">
      <p>
        Realize uma avaliação para receber recomendações
        personalizadas de estudo.
      </p>
    </div>
  ) : (
    <div className="plano-foco-lista">
      {[...desempenhosMaisRecentes]
        .sort((a, b) => {
          const ordem = { ALTA: 0, MEDIA: 1, BAIXA: 2 };

          return (
            (ordem[a.prioridade] ?? 3) -
              (ordem[b.prioridade] ?? 3) ||
            Number(a.percentual || 0) - Number(b.percentual || 0)
          );
        })
        .map((item) => (
          <article className="plano-foco-item" key={item.assuntoId}>
            <div className="plano-foco-informacoes">
              <span className="plano-foco-disciplina">
                {item.disciplinaNome}
              </span>

              <h3>{item.assuntoNome}</h3>

              <span className="plano-foco-desempenho">
                Desempenho: {Number(item.percentual || 0).toFixed(0)}%
              </span>
            </div>

            <div className="plano-foco-acoes">
              <span
                className={`plano-foco-prioridade prioridade-${String(
                  item.prioridade || "BAIXA"
                ).toLowerCase()}`}
              >
                {item.prioridade === "ALTA"
                  ? "Alta prioridade"
                  : item.prioridade === "MEDIA"
                  ? "Média prioridade"
                  : "Baixa prioridade"}
              </span>

              <button
                type="button"
                className="plano-foco-botao"
                onClick={() => setPaginaAtiva("materiais")}
              >
                Ver recomendações →
              </button>
            </div>
          </article>
        ))}
    </div>
  )}
</section>

      </>
    )}
  </section>
)}


{paginaAtiva === "evolucao" && (
  <section className="evolucao-pagina">
    <div className="evolucao-cabecalho">
      <span className="secao-etiqueta">ACOMPANHAMENTO ACADÊMICO</span>
      <h1>Minha Evolução</h1>
      <p>
        Acompanhe seu progresso ao longo das avaliações e descubra
        como seu desempenho está evoluindo.
      </p>
    </div>

    {carregandoHistorico ? (
      <p>Carregando sua evolução acadêmica...</p>
    ) : erroHistorico ? (
      <p className="materiais-erro">
        Não foi possível carregar seu histórico de avaliações.
      </p>
    ) : evolucaoDesempenho.length === 0 ? (
      <div className="evolucao-vazio">
        <h2>Sua jornada começa aqui!</h2>
        <p>
          Realize sua primeira avaliação para começar a acompanhar
          sua evolução acadêmica.
        </p>
        <button
          type="button"
          onClick={() => setPaginaAtiva("avaliacoes")}
        >
          Realizar avaliação
        </button>
      </div>
    ) : (
      <>
        <div className="evolucao-resumo">
          <article className="evolucao-indicador">
            <span>Avaliações realizadas</span>
            <strong>{evolucaoDesempenho.length}</strong>
          </article>

          <article className="evolucao-indicador">
            <span>Primeiro desempenho</span>
            <strong>
              {evolucaoDesempenho[0].percentual.toFixed(1)}%
            </strong>
          </article>

          <article className="evolucao-indicador">
            <span>Desempenho mais recente</span>
            <strong>
              {evolucaoDesempenho[
                evolucaoDesempenho.length - 1
              ].percentual.toFixed(1)}%
            </strong>
          </article>

          <article className="evolucao-indicador">
            <span>Variação no período</span>
            <strong>
              {(
                evolucaoDesempenho[
                  evolucaoDesempenho.length - 1
                ].percentual -
                evolucaoDesempenho[0].percentual
              ) > 0
                ? "+"
                : ""}
              {(
                evolucaoDesempenho[
                  evolucaoDesempenho.length - 1
                ].percentual -
                evolucaoDesempenho[0].percentual
              ).toFixed(1)} p.p.
            </strong>
          </article>
        </div>

        <div className="evolucao-secao">
  <h2>Evolução do desempenho por disciplina</h2>
  <p>
    Acompanhe seu desempenho geral e compare os resultados
    das disciplinas em que você está matriculado.
  </p>

  <div className="evolucao-legenda">
    <div className="evolucao-legenda-item">
      <span
        className="evolucao-legenda-cor"
        style={{ backgroundColor: "#16a34a" }}
      />
      <span>Desempenho geral</span>
    </div>

    {evolucaoPorDisciplina.map((disciplina, index) => {
      const cores = [
        "#2563eb",
        "#f97316",
        "#9333ea",
        "#dc2626",
        "#0891b2",
        "#ca8a04",
        "#db2777",
      ];

      return (
        <div
          className="evolucao-legenda-item"
          key={disciplina.disciplinaId}
        >
          <span
            className="evolucao-legenda-cor"
            style={{
              backgroundColor: cores[index % cores.length],
            }}
          />
          <span>{disciplina.disciplinaNome}</span>
        </div>
      );
    })}
  </div>


<div className="evolucao-grafico-container">
  <svg
    className="evolucao-grafico"
    viewBox="0 0 760 300"
    role="img"
    aria-label="Gráfico de evolução geral e por disciplina"
  >
    {[0, 25, 50, 75, 100].map((valor) => {
      const y = 250 - valor * 2;

      return (
        <g key={valor}>
          <line
            x1="65"
            y1={y}
            x2="730"
            y2={y}
            stroke="#e2e8f0"
            strokeDasharray="4 5"
          />
          <text
            x="50"
            y={y + 4}
            textAnchor="end"
            fontSize="12"
            fill="#64748b"
          >
            {valor}%
          </text>
        </g>
      );
    })}

    {(() => {
      const total = evolucaoDesempenho.length;
      const cores = [
        "#2563eb",
        "#f97316",
        "#9333ea",
        "#dc2626",
        "#0891b2",
        "#ca8a04",
        "#db2777",
      ];

      const obterX = (indice) =>
        total === 1 ? 397 : 65 + (indice * 665) / (total - 1);

      const obterY = (percentual) =>
        250 - Math.max(0, Math.min(100, percentual)) * 2;

      const pontosGerais = evolucaoDesempenho.map((item, indice) => {
        const avaliacoesAteAgora = evolucaoDesempenho.slice(0, indice + 1);
        const media =
          avaliacoesAteAgora.reduce(
            (soma, avaliacao) => soma + avaliacao.percentual,
            0
          ) / avaliacoesAteAgora.length;

        return {
          x: obterX(indice),
          y: obterY(media),
          percentual: media,
          numero: item.numero,
        };
      });

      return (
        <>
          {pontosGerais.length > 1 && (
            <polyline
              fill="none"
              stroke="#16a34a"
              strokeWidth="3"
              strokeLinecap="round"
              strokeLinejoin="round"
              points={pontosGerais
                .map((ponto) => `${ponto.x},${ponto.y}`)
                .join(" ")}
            />
          )}

          {evolucaoPorDisciplina.map((disciplina, indiceDisciplina) => {
            const cor = cores[indiceDisciplina % cores.length];


            const pontos = disciplina.avaliacoes.map((item, indice) => ({
            ...item,
            x: obterX(indice),
            y: obterY(item.percentual),
          }));


            return (
              <g key={disciplina.disciplinaId}>
                {pontos.length > 1 && (
                  <polyline
                    fill="none"
                    stroke={cor}
                    strokeWidth="3"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    points={pontos
                      .map((ponto) => `${ponto.x},${ponto.y}`)
                      .join(" ")}
                  />
                )}

                {pontos.map((ponto) => (
                  <circle
                    key={ponto.resultadoId}
                    cx={ponto.x}
                    cy={ponto.y}
                    r="5"
                    fill={cor}
                    stroke="#ffffff"
                    strokeWidth="2"
                  >
                    <title>
                      {`${disciplina.disciplinaNome}: ${ponto.percentual.toFixed(1)}%`}
                    </title>
                  </circle>
                ))}
              </g>
            );
          })}

          {pontosGerais.map((ponto) => (
            <g key={ponto.numero}>
              <circle
                cx={ponto.x}
                cy={ponto.y}
                r="4"
                fill="#16a34a"
                stroke="#ffffff"
                strokeWidth="1.5"
              >
                <title>
                  {`Média geral até a avaliação ${ponto.numero}: ${ponto.percentual.toFixed(1)}%`}
                </title>
              </circle>

              <text
                x={ponto.x}
                y="275"
                textAnchor="middle"
                fontSize="11"
                fill="#64748b"
              >
                {ponto.numero}
              </text>
            </g>
          ))}
        </>
      );
    })()}
  </svg>
</div>

          <p className="evolucao-grafico-legenda">
            Eixo horizontal: número da tentativa em cada disciplina. A linha geral representa a sequência de todas as avaliações.
          </p>
        </div>

        <div className="evolucao-secao">
          <h2>Evolução por assunto</h2>
          <p>
            Compare seu desempenho mais recente com a tentativa
            anterior de cada assunto.
          </p>

          {carregandoDesempenhos ? (
            <p>Carregando evolução por assunto...</p>
          ) : erroDesempenhos ? (
            <p className="materiais-erro">{erroDesempenhos}</p>
          ) : evolucaoPorAssunto.length === 0 ? (
            <p>Nenhum resultado por assunto disponível.</p>
          ) : (
            <div className="evolucao-assuntos-lista">
              {evolucaoPorAssunto.map((item) => (
                <article
                  className="evolucao-assunto-card"
                  key={item.assuntoId}
                >
                  <div>
                    <span className="evolucao-assunto-disciplina">
                      {item.disciplinaNome}
                    </span>
                    <h3>{item.assuntoNome}</h3>
                  </div>

                  <div className="evolucao-assunto-resultado">
                    <strong>
                      {Number(item.percentual).toFixed(2)}%
                    </strong>

                    <span>
                      {item.tendencia === "PRIMEIRA"
                        ? "Primeira avaliação"
                        : item.tendencia === "MELHOROU"
                        ? `↗ +${item.diferenca.toFixed(2)} p.p.`
                        : item.tendencia === "PIOROU"
                        ? `↘ ${item.diferenca.toFixed(2)} p.p.`
                        : `→ ${item.diferenca.toFixed(2)} p.p.`}
                    </span>
                  </div>
                </article>
              ))}
            </div>
          )}
        </div>
      </>
    )}
  </section>
)}


{paginaAtiva === "materiais" && (
  <div className="materiais-pagina">
    <section className="materiais-estudo">

<div className="materiais-recomendados">
  <div className="materiais-recomendados-cabecalho">
    <span className="secao-etiqueta">
      Aprendizagem personalizada
    </span>

    <h2>Recomendados para você</h2>

    <p>
      Identificamos os assuntos que precisam de mais atenção com base
      nos resultados das suas últimas avaliações.
    </p>
  </div>

  {evolucaoPorAssunto.filter(
    (item) => item.prioridade === "ALTA" || item.prioridade === "MEDIA"
  ).length === 0 ? (
    <p className="materiais-recomendados-vazio">
      Nenhum assunto com prioridade de reforço no momento.
    </p>
  ) : (
    <div className="materiais-recomendados-grid">
      {evolucaoPorAssunto
        .filter(
          (item) =>
            item.prioridade === "ALTA" ||
            item.prioridade === "MEDIA"
        )
        .sort(
          (a, b) =>
            Number(a.percentual) - Number(b.percentual)
        )
        .map((item) => (
          <article
            className="material-recomendado-card"
            key={item.assuntoId}
          >
            <div className="material-recomendado-topo">
              <span className="material-recomendado-disciplina">
                {item.disciplinaNome}
              </span>

              <span
                className={`diagnostico-prioridade ${
                  item.prioridade === "ALTA"
                    ? "prioridade-alta"
                    : "prioridade-media"
                }`}
              >
                {item.prioridade === "ALTA"
                  ? "Prioridade alta"
                  : "Prioridade média"}
              </span>
            </div>

            <h3>{item.assuntoNome}</h3>

            <p>
              Seu último desempenho foi de{" "}
              <strong>{Number(item.percentual).toFixed(2)}%</strong>.
              Recomendamos revisar este assunto para melhorar
              seus resultados.
            </p>

          </article>
        ))}
    </div>
  )}
</div>
</section>

<section className="materiais-estudo biblioteca-externa">

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

  {/^\/(works|books)\/OL\d+[WM]$/.test(livro.chave || "") && (
    <a
      href={`https://openlibrary.org${livro.chave}`}
      target="_blank"
      rel="noopener noreferrer"
      className="material-ver-livro"
    >
      Ver livro ↗
    </a>
  )}
</div>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
      )}

        {paginaAtiva === "desempenho" && (
  <section className="historico-avaliacoes">
    <div className="desempenho-cabecalho">
      <span className="secao-etiqueta">ANÁLISE ACADÊMICA</span>
      <h1>Análise e Diagnóstico de Desempenho</h1>
      <p>
        Identifique suas dificuldades, acompanhe seus resultados
        e descubra quais assuntos precisam de mais atenção.
      </p>
    </div>

    <div className="diagnostico-resumo">
      <article className="diagnostico-indicador">
        <span>Avaliações realizadas</span>
        <strong>{totalAvaliacoes}</strong>
      </article>

      <article className="diagnostico-indicador">
        <span>Desempenho médio</span>
        <strong>{desempenhoMedio.toFixed(1)}%</strong>
      </article>

      <article className="diagnostico-indicador">
        <span>Prioridade alta</span>
        <strong>{assuntosPrioridadeAlta}</strong>
      </article>

      <article className="diagnostico-indicador">
        <span>Prioridade média</span>
        <strong>{assuntosPrioridadeMedia}</strong>
      </article>
    </div>

    <div className="diagnostico-secao">
      <h2>Diagnóstico por assunto</h2>
      <p>
        Confira seu desempenho nos assuntos avaliados
        das disciplinas em que você está matriculado.
      </p>

      {carregandoDesempenhos && (
        <p>Carregando diagnóstico acadêmico...</p>
      )}

      {erroDesempenhos && (
        <p className="materiais-erro">{erroDesempenhos}</p>
      )}

      {!carregandoDesempenhos &&
        !erroDesempenhos &&
        desempenhosMaisRecentes.length === 0 && (
          <p>
            Nenhum diagnóstico disponível ainda.
            Finalize uma avaliação para começar.
          </p>
        )}

      {!carregandoDesempenhos &&
        desempenhosMaisRecentes.length > 0 && (
          <div className="diagnostico-lista">
            {evolucaoPorAssunto.map((item) => (
              <article
                className="diagnostico-card"
                key={item.assuntoId}
              >
                <div className="diagnostico-card-topo">
                  <div>
                    <span className="diagnostico-disciplina">
                      {item.disciplinaNome}
                    </span>
                    <h3>{item.assuntoNome}</h3>
                  </div>

                  <span
                    className={`diagnostico-prioridade ${
                      item.prioridade === "ALTA"
                        ? "prioridade-alta"
                        : item.prioridade === "MEDIA"
                        ? "prioridade-media"
                        : "prioridade-baixa"
                    }`}
                  >
                    {item.prioridade === "ALTA"
                      ? "Prioridade alta"
                      : item.prioridade === "MEDIA"
                      ? "Prioridade média"
                      : "Bom desempenho"}
                  </span>
                </div>

                <div className="diagnostico-resultado">
                  <strong>
                    {Number(item.percentual).toFixed(2)}%
                  </strong>

                  <span>
                    {item.acertos} acertos de {item.totalQuestoes} questões
                  </span>
                </div>

                <div className="diagnostico-barra">
                  <div
                    className="diagnostico-barra-progresso"
                    style={{
                      width: `${Math.max(
                        0,
                        Math.min(100, Number(item.percentual) || 0)
                      )}%`,
                    }}
                  />
                </div>

<div className={`diagnostico-evolucao evolucao-${item.tendencia.toLowerCase()}`}>
  {item.tendencia === "PRIMEIRA" ? (
    <span>Primeira avaliação — ainda não há comparação.</span>
  ) : (
    <>
      <div className="diagnostico-evolucao-indicador">
        <span className="diagnostico-evolucao-icone">
          {item.tendencia === "MELHOROU"
            ? "↗"
            : item.tendencia === "PIOROU"
            ? "↘"
            : "→"}
        </span>

        <strong>
          {item.diferenca > 0 ? "+" : ""}
          {item.diferenca.toFixed(2)} p.p.
        </strong>

        <span>
          {item.tendencia === "MELHOROU"
            ? "Melhorou"
            : item.tendencia === "PIOROU"
            ? "Precisa de atenção"
            : "Desempenho mantido"}
        </span>
      </div>

      <span className="diagnostico-evolucao-comparacao">
        Anterior: {item.percentualAnterior.toFixed(2)}% | Atual:{" "}
        {Number(item.percentual).toFixed(2)}%
      </span>
    </>
  )}
</div>

  <p className="diagnostico-recomendacao">
  {item.prioridade === "ALTA"
    ? item.tendencia === "MELHOROU"
      ? `Você está evoluindo em ${item.assuntoNome}! Seu desempenho melhorou ${item.diferenca.toFixed(2)} pontos percentuais, mas ainda é importante revisar os conceitos fundamentais e praticar novas questões.`
      : item.tendencia === "PIOROU"
      ? `Seu desempenho em ${item.assuntoNome} apresentou uma queda de ${Math.abs(item.diferenca).toFixed(2)} pontos percentuais. Recomendamos retomar os conteúdos básicos, revisar os erros e realizar novos exercícios antes da próxima avaliação.`
      : item.tendencia === "MANTEVE"
      ? `Seu desempenho em ${item.assuntoNome} permaneceu abaixo do esperado. Experimente novas estratégias de estudo, revise os principais conceitos e pratique exercícios diferentes.`
      : `Identificamos dificuldades em ${item.assuntoNome}. Priorize a revisão dos conceitos fundamentais e pratique exercícios para fortalecer sua aprendizagem.`
    : item.prioridade === "MEDIA"
    ? item.tendencia === "MELHOROU"
      ? `Ótima evolução em ${item.assuntoNome}! Continue praticando para consolidar seu conhecimento e alcançar um desempenho satisfatório.`
      : item.tendencia === "PIOROU"
      ? `Seu desempenho em ${item.assuntoNome} diminuiu. Recomendamos revisar os conteúdos em que encontrou dificuldades e reforçar a prática antes de uma nova avaliação.`
      : item.tendencia === "MANTEVE"
      ? `Seu desempenho em ${item.assuntoNome} está estável. Continue estudando e experimente exercícios mais desafiadores para avançar.`
      : `Você demonstrou conhecimento parcial em ${item.assuntoNome}. Continue praticando para fortalecer os pontos que precisam de atenção.`
    : item.tendencia === "MELHOROU"
    ? `Excelente progresso em ${item.assuntoNome}! Você alcançou um bom desempenho. Continue revisando periodicamente para manter seus resultados.`
    : item.tendencia === "PIOROU"
    ? `Apesar da queda em relação à avaliação anterior, seu desempenho em ${item.assuntoNome} continua satisfatório. Revise os erros para evitar novas dificuldades.`
    : item.tendencia === "MANTEVE"
    ? `Você manteve um bom desempenho em ${item.assuntoNome}. Continue praticando e explore conteúdos mais avançados.`
    : `Você apresentou um bom desempenho em ${item.assuntoNome}. Continue revisando para manter seu conhecimento.`}
  </p>

              </article>
            ))}
          </div>
        )}
    </div>
  </section>
)}

        {paginaAtiva === "avaliacoes" && !avaliacaoSelecionada && (
  <section className="avaliacoes-disponiveis">
    <div className="avaliacao-cabecalho">
      <div>
        <span className="secao-etiqueta">
          ÁREA DO ESTUDANTE
        </span>

        <h1>Minhas avaliações</h1>

        <p>
          Selecione uma avaliação para responder às questões.
        </p>
      </div>
    </div>

    {carregandoAvaliacoes ? (
      <p>Carregando avaliações...</p>
    ) : (
      <div className="grade-avaliacoes">
        {avaliacoesDisponiveis.filter((avaliacao) =>
          disciplinasMatriculadas.some(
            (disciplina) =>
              Number(disciplina.disciplinaId) ===
              Number(avaliacao.disciplinaId)
          )
        ).length === 0 ? (
          <p>Nenhuma avaliação disponível no momento.</p>
        ) : (
          avaliacoesDisponiveis
            .filter((avaliacao) =>
              disciplinasMatriculadas.some(
                (disciplina) =>
                  Number(disciplina.disciplinaId) ===
                  Number(avaliacao.disciplinaId)
              )
            )
            .map((avaliacao) => (
              <article
                className="card-avaliacao"
                key={avaliacao.id}
              >
                <h2>{avaliacao.disciplinaNome}</h2>

                <button
                  className="botao-principal"
                  type="button"
                  onClick={() => selecionarAvaliacao(avaliacao)}
                >
                  Iniciar avaliação
                </button>
              </article>
            ))
        )}
      </div>
    )}
  </section>
)}
        {paginaAtiva === "avaliacoes" && avaliacaoSelecionada && (
    <>
    <div className="voltar-avaliacoes">
      <button
        type="button"
        className="botao-voltar-avaliacoes"
        onClick={voltarParaAvaliacoes}
        disabled={enviando}
      >
        ← Voltar às avaliações
      </button>
    </div>

    <section className="avaliacao-cabecalho">
          <div>
            <span className="secao-etiqueta">
              {avaliacaoSelecionada.disciplinaNome}
            </span>

            <h1>{avaliacaoSelecionada.titulo}</h1>

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
  </>
)}
      </div>
    </main>
    {renderModalLegal()}
  </div>
);
}
export default App;
