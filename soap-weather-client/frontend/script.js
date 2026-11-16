const form = document.getElementById("weather-form");
const botao = document.getElementById("consultar");
const mensagem = document.getElementById("mensagem");
const resultado = document.getElementById("resultado");
const cardsDias = document.getElementById("cards-dias");
const coordenadas = document.getElementById("coordenadas");
const detalhesData = document.getElementById("detalhes-data");
const detalhesResumo = document.getElementById("detalhes-resumo");
const tabelaHorarios = document.getElementById("tabela-horarios");
const precipitacao = document.getElementById("precipitacao");

let previsoes = [];

form.addEventListener("submit", async function(event) {
  event.preventDefault();

  const latitude = document.getElementById("latitude").value;
  const longitude = document.getElementById("longitude").value;

  mensagem.classList.remove("erro");
  mensagem.textContent = "Consultando o Web Service SOAP...";
  botao.disabled = true;
  resultado.classList.add("oculto");

  try {
    const resposta = await fetch(`/api/weather?latitude=${latitude}&longitude=${longitude}`);

    if(!resposta.ok)
    {
      throw new Error("Não foi possível consultar a previsão.");
    }

    previsoes = await resposta.json();

    if(previsoes.length === 0)
    {
      throw new Error("Nenhuma previsão foi encontrada para estas coordenadas.");
    }

    coordenadas.textContent = `Latitude: ${latitude} | Longitude: ${longitude}`;

    montarCards();
    mostrarDetalhes(0);

    resultado.classList.remove("oculto");
    mensagem.textContent = "Previsão carregada com sucesso.";

    resultado.scrollIntoView({ behavior: "smooth" });
  }
  catch(erro)
  {
    mensagem.classList.add("erro");
    mensagem.textContent = erro.message;
  }
  finally
  {
    botao.disabled = false;
  }
});

function montarCards()
{
  cardsDias.innerHTML = "";

  previsoes.forEach(function(previsao, indice) {
    const card = document.createElement("div");
    card.classList.add("card-dia");

    if(indice === 0)
    {
      card.classList.add("ativo");
    }

    const maxima = previsao.maxima !== null ? `${previsao.maxima} °C` : "-";
    const minima = previsao.minima !== null ? `${previsao.minima} °C` : "-";

    card.innerHTML = `
      <p class="secao-tag">DIA</p>
      <h3>${previsao.data}</h3>

      <div class="card-temperaturas">
        <div>
          <span>Máxima</span>
          <strong>${maxima}</strong>
        </div>

        <div>
          <span>Mínima</span>
          <strong>${minima}</strong>
        </div>
      </div>
    `;

    card.addEventListener("click", function() {
      mostrarDetalhes(indice);
    });

    cardsDias.appendChild(card);
  });
}

function mostrarDetalhes(indice)
{
  const previsao = previsoes[indice];

  document.querySelectorAll(".card-dia").forEach(function(card, indiceCard) {
    if(indiceCard === indice)
    {
      card.classList.add("ativo");
    }
    else
    {
      card.classList.remove("ativo");
    }
  });

  detalhesData.textContent = previsao.data;

  const maxima = previsao.maxima !== null ? `${previsao.maxima} °C` : "Não disponível";
  const minima = previsao.minima !== null ? `${previsao.minima} °C` : "Não disponível";

  detalhesResumo.innerHTML = `
    <div class="resumo-box"><strong>Máxima:</strong> ${maxima}</div>
    <div class="resumo-box"><strong>Mínima:</strong> ${minima}</div>
  `;

  tabelaHorarios.innerHTML = "";

  previsao.horarios.forEach(function(item) {
    const linha = document.createElement("tr");

    const vento = item.vento !== null ? `${item.vento} m/s` : "-";

    linha.innerHTML = `
      <td>${item.hora}</td>
      <td>${item.temperatura} °C</td>
      <td>${vento}</td>
    `;

    tabelaHorarios.appendChild(linha);
  });

  precipitacao.innerHTML = "";

  if(previsao.precipitacao.length > 0)
  {
    const titulo = document.createElement("h4");
    titulo.textContent = "Probabilidade de precipitação";
    precipitacao.appendChild(titulo);

    previsao.precipitacao.forEach(function(item) {
      const elemento = document.createElement("div");
      elemento.classList.add("precipitacao-item");
      elemento.textContent = `${item.periodo} → ${item.probabilidade}%`;
      precipitacao.appendChild(elemento);
    });
  }
  else
  {
    precipitacao.innerHTML = "<p>Nenhum intervalo de precipitação disponível para este dia.</p>";
  }
}
