import { useEffect, useState } from "react";
import "./App.css";

function App() {
  const avaliacaoId = 1;
  const usuarioId = 1;

  const [questoes, setQuestoes] = useState([]);
  const [respostas, setRespostas] = useState({});
  const [resultado, setResultado] = useState(null);

  const [carregando, setCarregando] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState("");

  useEffect(() => {
    fetch(`http://localhost:8080/api/questoes/avaliacao/${avaliacaoId}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error("Erro ao buscar questões");
        }

        return response.json();
      })
      .then((dados) => {
        setQuestoes(dados);
        setCarregando(false);
      })
      .catch((error) => {
        console.error(error);
        setErro("Não foi possível carregar a avaliação.");
        setCarregando(false);
      });
  }, []);

  function selecionarResposta(questaoId, alternativa) {
    setRespostas((respostasAnteriores) => ({
      ...respostasAnteriores,
      [questaoId]: alternativa,
    }));
  }

  async function finalizarAvaliacao() {
    setErro("");

    if (Object.keys(respostas).length !== questoes.length) {
      setErro("Responda todas as questões antes de finalizar.");
      return;
    }

    const respostasFormatadas = questoes.map((questao) => ({
      questaoId: questao.id,
      resposta: respostas[questao.id],
    }));

    const dadosEnvio = {
      usuarioId: usuarioId,
      avaliacaoId: avaliacaoId,
      respostas: respostasFormatadas,
    };

    try {
      setEnviando(true);

      const response = await fetch(
        "http://localhost:8080/api/avaliacoes/finalizar",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(dadosEnvio),
        }
      );

      if (!response.ok) {
        throw new Error("Erro ao finalizar avaliação");
      }

      const dadosResultado = await response.json();

      setResultado(dadosResultado);
    } catch (error) {
      console.error(error);
      setErro("Não foi possível finalizar a avaliação.");
    } finally {
      setEnviando(false);
    }
  }

  if (carregando) {
    return (
      <div className="container">
        <p>Carregando avaliação...</p>
      </div>
    );
  }

  if (resultado) {
    return (
      <div className="container">
        <header>
          <h1>SAP</h1>
          <p>
            Sistema de Acompanhamento e Personalização da Aprendizagem
          </p>
        </header>

        <main className="resultado">
          <h2>Resultado da avaliação</h2>

          <div className="percentual">
            {resultado.percentual}%
          </div>

          <p>
            Total de questões:{" "}
            <strong>{resultado.totalQuestoes}</strong>
          </p>

          <p>
            Acertos: <strong>{resultado.acertos}</strong>
          </p>

          <p>
            Erros: <strong>{resultado.erros}</strong>
          </p>

          <div className="analise-desempenho">
            <h3>Análise de desempenho</h3>

            <p>
              Nível de dificuldade:{" "}
              <strong>{resultado.dificuldade}</strong>
            </p>

            <p>
              Prioridade de estudo:{" "}
              <strong>{resultado.prioridade}</strong>
            </p>

            <p>
              Recomendação:
            </p>

            <p>
              {resultado.recomendacao}
            </p>
          </div>


          <button
            onClick={() => {
              setResultado(null);
              setRespostas({});
            }}
          >
            Refazer avaliação
          </button>
        </main>
      </div>
    );
  }

  return (
    <div className="container">
      <header>
        <h1>SAP</h1>

        <p>
          Sistema de Acompanhamento e Personalização da Aprendizagem
        </p>
      </header>

      <main className="avaliacao">
        <h2>Avaliação de SQL</h2>

        <p className="subtitulo">
          Responda às questões abaixo e finalize a avaliação.
        </p>

        {questoes.map((questao, index) => (
          <div className="questao" key={questao.id}>
            <h3>Questão {index + 1}</h3>

            <p className="enunciado">
              {questao.enunciado}
            </p>

            <label>
              <input
                type="radio"
                name={`questao-${questao.id}`}
                value="A"
                checked={respostas[questao.id] === "A"}
                onChange={() =>
                  selecionarResposta(questao.id, "A")
                }
              />
              A) {questao.alternativaA}
            </label>

            <label>
              <input
                type="radio"
                name={`questao-${questao.id}`}
                value="B"
                checked={respostas[questao.id] === "B"}
                onChange={() =>
                  selecionarResposta(questao.id, "B")
                }
              />
              B) {questao.alternativaB}
            </label>

            <label>
              <input
                type="radio"
                name={`questao-${questao.id}`}
                value="C"
                checked={respostas[questao.id] === "C"}
                onChange={() =>
                  selecionarResposta(questao.id, "C")
                }
              />
              C) {questao.alternativaC}
            </label>

            <label>
              <input
                type="radio"
                name={`questao-${questao.id}`}
                value="D"
                checked={respostas[questao.id] === "D"}
                onChange={() =>
                  selecionarResposta(questao.id, "D")
                }
              />
              D) {questao.alternativaD}
            </label>
          </div>
        ))}

        {erro && <p className="erro">{erro}</p>}

        <button
          onClick={finalizarAvaliacao}
          disabled={enviando}
        >
          {enviando
            ? "Finalizando..."
            : "Finalizar avaliação"}
        </button>
      </main>
    </div>
  );
}

export default App;