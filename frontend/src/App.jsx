import { useEffect, useState } from "react";
import "./App.css";

function App() {
  const [questoes, setQuestoes] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");

  useEffect(() => {
    fetch("http://localhost:8080/api/questoes/avaliacao/1")
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

  if (carregando) {
    return (
      <div className="container">
        <p>Carregando avaliação...</p>
      </div>
    );
  }

  if (erro) {
    return (
      <div className="container">
        <p>{erro}</p>
      </div>
    );
  }

  return (
    <div className="container">
      <header>
        <h1>SAP</h1>
        <p>Sistema de Acompanhamento e Personalização da Aprendizagem</p>
      </header>

      <main className="avaliacao">
        <h2>Avaliação de SQL</h2>

        <p className="subtitulo">
          Responda às questões abaixo e finalize a avaliação.
        </p>

        {questoes.map((questao, index) => (
          <div className="questao" key={questao.id}>
            <h3>
              Questão {index + 1}
            </h3>

            <p className="enunciado">
              {questao.enunciado}
            </p>

            <label>
              <input
                type="radio"
                name={`questao-${questao.id}`}
                value="A"
              />
              A) {questao.alternativaA}
            </label>

            <label>
              <input
                type="radio"
                name={`questao-${questao.id}`}
                value="B"
              />
              B) {questao.alternativaB}
            </label>

            <label>
              <input
                type="radio"
                name={`questao-${questao.id}`}
                value="C"
              />
              C) {questao.alternativaC}
            </label>

            <label>
              <input
                type="radio"
                name={`questao-${questao.id}`}
                value="D"
              />
              D) {questao.alternativaD}
            </label>
          </div>
        ))}

        <button>
          Finalizar avaliação
        </button>
      </main>
    </div>
  );
}

export default App;