// JavaScript customizado para o Sistema de Terminais
// Função para confirmar exclusão
function confirmarExclusao(mensagem) {
    return confirm(mensagem || 'Deseja realmente excluir este item?');
}

// Função para auto-dismiss de alerts
document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss alerts após 5 segundos
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    })

// Validação de formulários
const forms = document.querySelectorAll('.needs-validation');
forms.forEach(function (form) {
    form.addEventListener('submit', function (event) {
        if (!form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
        }
        form.classList.add('was-validated');
    });
});

// Máscara para campos de moeda
const camposMoeda = document.querySelectorAll('input[data-mask="currency"]');
camposMoeda.forEach(function (campo) {
    campo.addEventListener('input', function (e) {
        let value = e.target.value.replace(/\D/g, '');
        value = (value / 100).toFixed(2);
        e.target.value = value;
    });
});

// Inicializar funções específicas
atualizarUrlTerminal();
validarDatasAgendamento();
implementarBuscaGlobal();
inicializarTooltips();
configurarMascaras();
});

// Função para atualizar URL do terminal em tempo real
function atualizarUrlTerminal() {
    const categoriaSelect = document.getElementById('categoria');
    const numeroInput = document.getElementById('numero');
    const urlPreview = document.getElementById('url-preview');
    if (categoriaSelect && numeroInput) {
        function updateUrl() {
            const categoriaText = categoriaSelect.options[categoriaSelect.selectedIndex]?.text;
            const numero = numeroInput.value;

            if (categoriaText && numero) {
                const categoriaFormatada = categoriaText.toLowerCase().replace(/\s+/g, '');
                const url = `localhost:8080/api-mercado/api/${categoriaFormatada}/${numero}`;

                // Atualizar preview se existir
                if (urlPreview) {
                    urlPreview.textContent = url;
                    urlPreview.style.display = 'block';
                }

                // Atualizar campo hidden se existir
                const urlHidden = document.getElementById('url');
                if (urlHidden) {
                    urlHidden.value = `/${categoriaFormatada}/${numero}`;
                }

                // Mostrar alert de sucesso
                mostrarAlertTemporario('URL gerada: ' + url, 'info');

            } else {
                if (urlPreview) {
                    urlPreview.style.display = 'none';
                }
            }
        }

        categoriaSelect.addEventListener('change', updateUrl);
        numeroInput.addEventListener('input', updateUrl);
        // Executar na inicialização
        updateUrl();
    }
}

// Função para validar datas de agendamento
function validarDatasAgendamento() {
    const dataInicio = document.getElementById('dataInicio');
    const dataFim = document.getElementById('dataFim');
    if (dataInicio && dataFim) {
        function validarDatas() {
            const inicio = new Date(dataInicio.value);
            const fim = new Date(dataFim.value);
            const agora = new Date();

            // Limpar validações anteriores
            dataInicio.setCustomValidity('');
            dataFim.setCustomValidity('');
            /*alert(dataInicio.value);
            alert(dataFim.value);
            alert(inicio);
            alert(fim);
            alert(agora);*/
            if (dataInicio.value && inicio < agora) {
                dataInicio.setCustomValidity('A data de início deve ser no futuro');
                mostrarAlertTemporario('A data de início deve ser no futuro', 'warning');
            } else if (dataInicio.value && dataFim.value && fim <= inicio) {
                dataFim.setCustomValidity('A data de fim deve ser posterior à data de início');
                mostrarAlertTemporario('A data de fim deve ser posterior à data de início', 'warning');
            }
            /*alert('v1');*/
            // Verificar se o período é muito longo (mais de 8 horas)
            if (dataInicio.value && dataFim.value && (fim - inicio) > 8 * 60 * 60 * 1000) {
                alert('Agendamentos não podem exceder 8 horas');
                mostrarAlertTemporario('Agendamentos não podem exceder 8 horas', 'info');
            }
        }

        dataInicio.addEventListener('change', validarDatas);
        dataFim.addEventListener('change', validarDatas);
        dataInicio.addEventListener('blur', validarDatas);
        dataFim.addEventListener('blur', validarDatas);
    }
}

// Função para busca em tempo real
function implementarBusca(inputId, tabelaId) {
    const input = document.getElementById(inputId);
    const tabela = document.getElementById(tabelaId);
    if (input && tabela) {
        input.addEventListener('keyup', function () {
            const filtro = this.value.toLowerCase();
            const tbody = tabela.getElementsByTagName('tbody')[0];
            const linhas = tbody.getElementsByTagName('tr');
            let resultados = 0;
            for (let i = 0; i < linhas.length; i++) {
                const linha = linhas[i];
                const texto = linha.textContent.toLowerCase();
                if (texto.includes(filtro)) {
                    linha.style.display = '';
                    resultados++;
                } else {
                    linha.style.display = 'none';
                }
            }

            // Mostrar mensagem se não houver resultados
            let msgRow = tbody.querySelector('.no-results-row');
            if (resultados === 0 && filtro !== '') {
                if (!msgRow) {
                    msgRow = document.createElement('tr');
                    msgRow.className = 'no-results-row';
                    msgRow.innerHTML = '<td colspan="100%" class="text-center text-muted">Nenhum resultado encontrado</td>';
                    tbody.appendChild(msgRow);
                }
                msgRow.style.display = '';
            } else if (msgRow) {
                msgRow.style.display = 'none';
            }
        });
    }
}

// Função para busca global (implementar em todas as páginas de lista)
function implementarBuscaGlobal() {
    const searchInput = document.querySelector('[data-search]');
    const table = document.querySelector('.table');
    if (searchInput && table) {
        implementarBusca(searchInput.id, table.id || 'main-table');
        table.id = table.id || 'main-table';
    }
}

// Função para mostrar alerts temporários
function mostrarAlertTemporario(mensagem, tipo = 'info', duracao = 3000) {
    const alertContainer = document.querySelector('.alert-container') || document.body;
    const alert = document.createElement('div');
    alert.className = `alert alert-${tipo} alert-dismissible fade show position-fixed`;
    alert.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    alert.innerHTML = `        ${mensagem}        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>    `;
    alertContainer.appendChild(alert);

    // Auto-remover após duração especificada
    setTimeout(() => {
        if (alert.parentNode) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }
    }, duracao);
}

// Função para atualizar dados em tempo real
function atualizarDadosTempoReal() {

    return false;

    // Atualizar contador de visualizações a cada 30 segundos
    setInterval(function () {
        fetch('/api-mercado/api/stats').then(response => {
            if (!response.ok) throw new Error('Erro na requisição');
            return response.json();
        }).then(data => {
            const contadores = document.querySelectorAll('[data-counter]');
            contadores.forEach(contador => {
                const tipo = contador.getAttribute('data-counter');
                if (data[tipo] !== undefined) {
                    animarContador(contador, data[tipo]);
                }
            });
        }).catch(error => {
            console.log('Erro ao atualizar dados:', error);
        });
    }, 30000);
}

// Função para animar contadores
function animarContador(elemento, novoValor) {
    const valorAtual = parseInt(elemento.textContent) || 0;
    const diferenca = novoValor - valorAtual;
    if (diferenca !== 0) {
        let contador = valorAtual;
        const incremento = diferenca / 20;
        // 20 frames de animação
        const timer = setInterval(() => {
            contador += incremento;
            elemento.textContent = Math.round(contador);
            if ((incremento > 0 && contador >= novoValor) || (incremento < 0 && contador <= novoValor)) {
                elemento.textContent = novoValor;
                clearInterval(timer);
            }
        }, 50);
    }
}

// Função para inicializar tooltips
function inicializarTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Função para configurar máscaras de entrada
function configurarMascaras() {
    // Máscara para código de barras
    const codigoBarras = document.querySelectorAll('input[name="codigoBarras"]');
    codigoBarras.forEach(input => {
        input.addEventListener('input', function (e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 13) value = value.substring(0, 13);
            e.target.value = value;
        });
    });

    // Máscara para preço
    const precoInputs = document.querySelectorAll('input[name="preco"]');
    precoInputs.forEach(input => {
        input.addEventListener('blur', function (e) {
            let value = parseFloat(e.target.value);
            if (!isNaN(value)) {
                e.target.value = value.toFixed(2);
            }
        });
    });

    // Máscara para estoque (apenas números)
    const estoqueInputs = document.querySelectorAll('input[name="estoque"]');
    estoqueInputs.forEach(input => {
        input.addEventListener('input', function (e) {
            e.target.value = e.target.value.replace(/\D/g, '');
        });
    });
}

// Função para confirmar ações críticas
function confirmarAcao(elemento, mensagem, callback) {
    elemento.addEventListener('click', function (e) {
        e.preventDefault();
        if (confirm(mensagem)) {
            if (callback && typeof callback === 'function') {
                callback();
            } else {
                window.location.href = elemento.href;
            }
        }
    });
}

// Função para validar formulários em tempo real
function validarFormulario(formId) {
    const form = document.getElementById(formId);
    if (!form) return;
    const inputs = form.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
        input.addEventListener('blur', function () {
            validarCampo(input);
        });
        input.addEventListener('input', function () {
            if (input.classList.contains('is-invalid')) {
                validarCampo(input);
            }
        });
    });
}

// Função para validar campo individual
function validarCampo(input) {
    const valor = input.value.trim();
    let valido = true;
    let mensagem = '';

    // Validações específicas por tipo
    switch (input.type) {
        case 'email':
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (valor && !emailRegex.test(valor)) {
                valido = false;
                mensagem = 'Email inválido';
            }
            break;
        case 'number':
            if (valor && isNaN(valor)) {
                valido = false;
                mensagem = 'Deve ser um número válido';
            }
            break;
        case 'url':
            try {
                if (valor) new URL(valor);
            } catch {
                valido = false;
                mensagem = 'URL inválida';
            }
            break;
    }
    // Validação de campos obrigatórios
    if (input.hasAttribute('required') && !valor) {
        valido = false;
        mensagem = 'Campo obrigatório';
    }

    // Aplicar classes CSS
    if (valido) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
    } else {
        input.classList.remove('is-valid');
        input.classList.add('is-invalid');
    }

    // Mostrar/esconder mensagem de erro
    let feedback = input.nextElementSibling;
    if (feedback && feedback.classList.contains('invalid-feedback')) {
        feedback.textContent = mensagem;
    }
}

// Função para loading states
function mostrarLoading(elemento, texto = 'Carregando...') {
    const loadingHtml = `        <span class="spinner-border spinner-border-sm me-2" role="status"></span>        ${texto}    `;
    elemento.dataset.originalHtml = elemento.innerHTML;
    elemento.innerHTML = loadingHtml;
    elemento.disabled = true;
}

function esconderLoading(elemento) {
    if (elemento.dataset.originalHtml) {
        elemento.innerHTML = elemento.dataset.originalHtml;
        delete elemento.dataset.originalHtml;
    }
    elemento.disabled = false;
}

// Função para salvar formulário via AJAX
function salvarFormularioAjax(formId, successCallback, errorCallback) {
    const form = document.getElementById(formId);
    if (!form) return;
    form.addEventListener('submit', function (e) {
        e.preventDefault();
        const submitBtn = form.querySelector('button[type="submit"]');
        mostrarLoading(submitBtn, 'Salvando...');
        const formData = new FormData(form);
        fetch(form.action, {method: 'POST', body: formData}).then(response => {
            if (!response.ok) throw new Error('Erro no servidor');
            return response.json();
        }).then(data => {
            esconderLoading(submitBtn);
            mostrarAlertTemporario('Dados salvos com sucesso!', 'success');
            if (successCallback) successCallback(data);
        }).catch(error => {
            esconderLoading(submitBtn);
            mostrarAlertTemporario('Erro ao salvar dados', 'danger');
            if (errorCallback) errorCallback(error);
        });
    });
}

// Função para testar conexão com API
function testarConexaoApi() {
    fetch('/api-mercado/api/terminais').then(response => {
        if (response.ok) {
            mostrarAlertTemporario('Conexão com API funcionando', 'success', 2000);
        } else {
            throw new Error('API não respondeu');
        }
    }).catch(error => {
        mostrarAlertTemporario('Erro na conexão com API', 'warning', 3000);
        console.error('Erro na API:', error);
    });
}

// Função para copiar texto para clipboard
function copiarParaClipboard(texto, mensagem = 'Texto copiado!') {
    navigator.clipboard.writeText(texto).then(function () {
        mostrarAlertTemporario(mensagem, 'info', 1500);
    }).catch(function () {
        // Fallback para navegadores antigos
        const textarea = document.createElement('textarea');
        textarea.value = texto;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        mostrarAlertTemporario(mensagem, 'info', 1500);
    });
}

// Função para adicionar botões de cópia nas URLs
function adicionarBotoesCopia() {
    const urls = document.querySelectorAll('code');
    urls.forEach(url => {
        if (url.textContent.includes('localhost:8080')) {
            const botao = document.createElement('button');
            botao.className = 'btn btn-sm btn-outline-secondary ms-1';
            botao.innerHTML = '<i class="fas fa-copy"></i>';
            botao.onclick = () => copiarParaClipboard(url.textContent, 'URL copiada!');
            url.parentNode.insertBefore(botao, url.nextSibling);
        }
    });
}

// Função para atualizar timestamp relativo
function atualizarTimestamps() {
    const timestamps = document.querySelectorAll('[data-timestamp]');
    timestamps.forEach(element => {
        const timestamp = new Date(element.dataset.timestamp);
        const agora = new Date();
        const diferenca = agora - timestamp;
        let texto = '';
        if (diferenca < 60000) {
            texto = 'Agora mesmo';
        } else if (diferenca < 3600000) {
            texto = Math.floor(diferenca / 60000) + ' minutos atrás';
        } else if (diferenca < 86400000) {
            texto = Math.floor(diferenca / 3600000) + ' horas atrás';
        } else {
            texto = Math.floor(diferenca / 86400000) + ' dias atrás';
        }
        element.textContent = texto;
    });
}

// Inicializar quando o DOM estiver pronto
document.addEventListener('DOMContentLoaded', function () {
    // Executar funções de inicialização
    atualizarDadosTempoReal();
    adicionarBotoesCopia();
    // Atualizar timestamps a cada minuto
    setInterval(atualizarTimestamps, 60000);
    atualizarTimestamps(); // Executar imediatamente
// Testar API na inicialização (apenas em páginas administrativas)
    if (window.location.pathname.includes('/admin') || window.location.pathname.includes('/home')) {
        setTimeout(testarConexaoApi, 2000);
    }
});

// Função para debug (remover em produção)
function debug(objeto, titulo = 'Debug') {
    if (window.location.hostname === 'localhost') {
        console.group(titulo);
        console.log(objeto);
        console.groupEnd();
    }
}

// Exportar funções para uso global
window.SistemaTerminais = {
    confirmarExclusao,
    mostrarAlertTemporario,
    atualizarUrlTerminal,
    validarDatasAgendamento,
    implementarBusca,
    salvarFormularioAjax,
    copiarParaClipboard,
    mostrarLoading,
    esconderLoading,
    testarConexaoApi
};