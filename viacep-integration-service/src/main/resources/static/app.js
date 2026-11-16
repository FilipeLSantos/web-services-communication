(() => {
    const HISTORY_KEY = "viacep_recent_searches";
    const MAX_HISTORY = 4;

    const tabs = document.querySelectorAll(".tab");
    const formCep = document.getElementById("form-cep");
    const formAddress = document.getElementById("form-address");

    const inputCep = document.getElementById("input-cep");
    const clearCepBtn = document.getElementById("clear-cep");
    const inputUf = document.getElementById("input-uf");
    const inputCidade = document.getElementById("input-cidade");
    const inputLogradouro = document.getElementById("input-logradouro");

    const submitCepBtn = document.getElementById("submit-cep");
    const submitAddressBtn = document.getElementById("submit-address");

    const errorBanner = document.getElementById("error-banner");
    const errorMessage = document.getElementById("error-message");

    const resultCard = document.getElementById("result-card");
    const resCep = document.getElementById("res-cep");
    const resLogradouro = document.getElementById("res-logradouro");
    const resBairro = document.getElementById("res-bairro");
    const resCidadeUf = document.getElementById("res-cidade-uf");

    const copyCepBtn = document.getElementById("copy-cep");
    const copyAllBtn = document.getElementById("copy-all");

    const historySection = document.getElementById("history-section");
    const historyList = document.getElementById("history-list");
    const clearHistoryBtn = document.getElementById("clear-history");

    let currentResult = null;

    function formatCepMask(value) {
        const digits = value.replace(/\D/g, "").slice(0, 8);
        if (digits.length > 5) {
            return `${digits.slice(0, 5)}-${digits.slice(5)}`;
        }
        return digits;
    }

    function showError(message) {
        errorMessage.textContent = message;
        errorBanner.hidden = false;
    }

    function hideError() {
        errorBanner.hidden = true;
        errorMessage.textContent = "";
    }

    function setLoading(button, isLoading, defaultLabel) {
        const textEl = button.querySelector(".btn-text");
        const spinnerEl = button.querySelector(".spinner");
        button.disabled = isLoading;
        textEl.textContent = isLoading ? "Buscando..." : defaultLabel;
        spinnerEl.hidden = !isLoading;
    }

    function switchMode(mode) {
        hideError();
        tabs.forEach((tab) => {
            const active = tab.dataset.mode === mode;
            tab.classList.toggle("is-active", active);
            tab.setAttribute("aria-selected", String(active));
        });

        const isCep = mode === "cep";
        formCep.hidden = !isCep;
        formAddress.hidden = isCep;

        if (isCep) {
            inputCep.focus();
        } else {
            inputUf.focus();
        }
    }

    function renderResult(data) {
        currentResult = data;
        resCep.textContent = data.cep || "—";
        resLogradouro.textContent = data.logadouro || "Não informado (CEP único)";
        resBairro.textContent = data.bairro || "Não informado";
        resCidadeUf.textContent =
            data.cidade && data.estado ? `${data.cidade} — ${data.estado}` : data.cidade || data.estado || "—";
        resultCard.hidden = false;
    }

    async function copyToClipboard(text, button, defaultLabel) {
        if (!text) return;
        try {
            await navigator.clipboard.writeText(text);
            button.textContent = "Copiado!";
            button.classList.add("is-copied");
            setTimeout(() => {
                button.textContent = defaultLabel;
                button.classList.remove("is-copied");
            }, 1600);
        } catch {
            showError("Não foi possível copiar para a área de transferência.");
        }
    }

    function loadHistory() {
        try {
            const raw = localStorage.getItem(HISTORY_KEY);
            return raw ? JSON.parse(raw) : [];
        } catch {
            return [];
        }
    }

    function saveHistory(entry) {
        const items = loadHistory().filter((item) => item.label !== entry.label);
        items.unshift(entry);
        const trimmed = items.slice(0, MAX_HISTORY);
        try {
            localStorage.setItem(HISTORY_KEY, JSON.stringify(trimmed));
        } catch {
            // Ignore storage quota errors
        }
        renderHistory();
    }

    function renderHistory() {
        const items = loadHistory();
        historyList.innerHTML = "";
        if (items.length === 0) {
            historySection.hidden = true;
            return;
        }

        items.forEach((item) => {
            const chip = document.createElement("button");
            chip.type = "button";
            chip.className = "history-chip";
            chip.textContent = item.label;
            chip.addEventListener("click", () => {
                if (item.mode === "cep") {
                    switchMode("cep");
                    inputCep.value = formatCepMask(item.payload.cep);
                    clearCepBtn.hidden = !inputCep.value;
                    executeCepSearch(item.payload.cep);
                } else {
                    switchMode("address");
                    inputUf.value = item.payload.uf;
                    inputCidade.value = item.payload.cidade;
                    inputLogradouro.value = item.payload.logadouro;
                    executeAddressSearch(item.payload);
                }
            });
            historyList.appendChild(chip);
        });

        historySection.hidden = false;
    }

    async function executeCepSearch(rawCep) {
        const digits = rawCep.replace(/\D/g, "");
        if (digits.length !== 8) {
            showError("Digite um CEP válido com 8 dígitos.");
            inputCep.focus();
            return;
        }

        hideError();
        setLoading(submitCepBtn, true, "Consultar CEP");

        try {
            const response = await fetch("/client/cep", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ cep: digits }),
            });

            const data = await response.json();
            if (!response.ok) {
                resultCard.hidden = true;
                showError(data.message || "CEP inválido ou não encontrado.");
                return;
            }

            renderResult(data);
            saveHistory({
                mode: "cep",
                label: data.cep || formatCepMask(digits),
                payload: { cep: digits },
            });
        } catch {
            showError("Erro de conexão com o servidor. Verifique se a API está em execução.");
        } finally {
            setLoading(submitCepBtn, false, "Consultar CEP");
        }
    }

    async function executeAddressSearch({ uf, cidade, logadouro }) {
        if (!uf) {
            showError("Selecione um estado (UF).");
            inputUf.focus();
            return;
        }
        if (cidade.trim().length < 3) {
            showError("Informe a cidade com pelo menos 3 caracteres.");
            inputCidade.focus();
            return;
        }
        if (logadouro.trim().length < 3) {
            showError("Informe o logradouro com pelo menos 3 caracteres.");
            inputLogradouro.focus();
            return;
        }

        hideError();
        setLoading(submitAddressBtn, true, "Buscar Endereço");

        try {
            const response = await fetch("/client/address", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    uf: uf.trim().toUpperCase(),
                    cidade: cidade.trim(),
                    logadouro: logadouro.trim(),
                }),
            });

            const data = await response.json();
            if (!response.ok) {
                resultCard.hidden = true;
                showError(data.message || "Endereço não encontrado.");
                return;
            }

            renderResult(data);
            saveHistory({
                mode: "address",
                label: `${logadouro.trim()}, ${cidade.trim()} - ${uf}`,
                payload: {
                    uf: uf.trim().toUpperCase(),
                    cidade: cidade.trim(),
                    logadouro: logadouro.trim(),
                },
            });
        } catch {
            showError("Erro de conexão com o servidor. Verifique se a API está em execução.");
        } finally {
            setLoading(submitAddressBtn, false, "Buscar Endereço");
        }
    }

    // Event Listeners
    tabs.forEach((tab) => {
        tab.addEventListener("click", () => switchMode(tab.dataset.mode));
    });

    inputCep.addEventListener("input", (event) => {
        const masked = formatCepMask(event.target.value);
        event.target.value = masked;
        clearCepBtn.hidden = masked.length === 0;
        if (!errorBanner.hidden) hideError();
    });

    clearCepBtn.addEventListener("click", () => {
        inputCep.value = "";
        clearCepBtn.hidden = true;
        hideError();
        inputCep.focus();
    });

    formCep.addEventListener("submit", (event) => {
        event.preventDefault();
        executeCepSearch(inputCep.value);
    });

    formAddress.addEventListener("submit", (event) => {
        event.preventDefault();
        executeAddressSearch({
            uf: inputUf.value,
            cidade: inputCidade.value,
            logadouro: inputLogradouro.value,
        });
    });

    copyCepBtn.addEventListener("click", () => {
        if (currentResult?.cep) {
            copyToClipboard(currentResult.cep, copyCepBtn, "Copiar CEP");
        }
    });

    copyAllBtn.addEventListener("click", () => {
        if (!currentResult) return;
        const parts = [
            currentResult.logadouro,
            currentResult.bairro,
            currentResult.cidade && currentResult.estado
                ? `${currentResult.cidade} - ${currentResult.estado}`
                : currentResult.cidade || currentResult.estado,
            currentResult.cep ? `CEP ${currentResult.cep}` : "",
        ].filter(Boolean);
        copyToClipboard(parts.join(", "), copyAllBtn, "Copiar tudo");
    });

    clearHistoryBtn.addEventListener("click", () => {
        localStorage.removeItem(HISTORY_KEY);
        renderHistory();
    });

    renderHistory();
})();
