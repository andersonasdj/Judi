// ── PASSWORD GENERATOR ──────────────────────────────────────────────────────

// Injeta header e estrutura moderna antes de qualquer query
(function injectModernLayout() {
    var container = document.querySelector('.container-password');
    if (!container) return;

    // 1. Header
    var header = document.createElement('div');
    header.className = 'pg-header';
    header.innerHTML =
        '<div class="pg-header-left">' +
            '<div class="pg-header-icon"><i class="bi bi-key-fill"></i></div>' +
            '<div><div class="pg-title">Password Generator</div>' +
            '<div class="pg-subtitle">Crie senhas seguras e aleatórias</div></div>' +
        '</div>' +
        '<button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Fechar"></button>';
    container.insertBefore(header, container.firstChild);

    // 2. Envolve conteúdo existente em .pg-body
    var body = document.createElement('div');
    body.className = 'pg-body';
    var children = Array.from(container.childNodes).filter(function(n) { return n !== header; });
    children.forEach(function(c) { body.appendChild(c); });
    container.appendChild(body);

    // 3. Barra de força após .result
    var resultDiv = body.querySelector('.result');
    if (resultDiv) {
        var strengthWrap = document.createElement('div');
        strengthWrap.className = 'pg-strength-wrap';
        strengthWrap.innerHTML =
            '<div class="pg-strength-header">' +
                '<span class="pg-strength-label">Força</span>' +
                '<span class="pg-strength-text" id="strengthText"></span>' +
            '</div>' +
            '<div class="pg-strength-track"><div class="pg-strength-bar" id="strengthBar"></div></div>';
        resultDiv.insertAdjacentElement('afterend', strengthWrap);
    }

    // 4. Label + badge de tamanho acima do slider
    var sliderWrap = body.querySelector('.length.range__slider');
    if (sliderWrap) {
        var sliderSection = document.createElement('div');
        var sliderRow = document.createElement('div');
        sliderRow.className = 'pg-slider-row';
        sliderRow.innerHTML =
            '<span class="pg-slider-label">Tamanho</span>' +
            '<span class="pg-size-badge" id="pgSizeBadge">16</span>';
        sliderWrap.parentNode.insertBefore(sliderSection, sliderWrap);
        sliderSection.appendChild(sliderRow);
        sliderSection.appendChild(sliderWrap);
    }

    // 5. Label "Incluir" acima das opções
    var settingsDiv = body.querySelector('.settings');
    if (settingsDiv) {
        var optLabel = document.createElement('div');
        optLabel.className = 'pg-options-label';
        optLabel.textContent = 'Incluir';
        settingsDiv.parentNode.insertBefore(optLabel, settingsDiv);
    }
})();

// ── STRENGTH (declarado antes do slider para evitar referência antecipada) ────
var strengthConfig = [
    { label: '',            color: '#e2e8f0', pct: 0   },
    { label: 'Muito Fraca', color: '#ef4444', pct: 16  },
    { label: 'Fraca',       color: '#f97316', pct: 32  },
    { label: 'Razoável',    color: '#eab308', pct: 50  },
    { label: 'Boa',         color: '#3b82f6', pct: 66  },
    { label: 'Forte',       color: '#22c55e', pct: 82  },
    { label: 'Muito Forte', color: '#16a34a', pct: 100 },
];

function updateStrength() {
    var bar  = document.getElementById('strengthBar');
    var text = document.getElementById('strengthText');
    if (!bar || !text) return;

    var checks = [
        document.getElementById('uppercase'),
        document.getElementById('lowercase'),
        document.getElementById('number'),
        document.getElementById('symbol'),
    ].filter(function(el) { return el && el.checked; }).length;

    var sliderEl = document.getElementById('slider');
    var len = sliderEl ? +sliderEl.value : 16;
    var score = 0;
    if (len >= 8)    score++;
    if (len >= 12)   score++;
    if (len >= 16)   score++;
    if (checks >= 2) score++;
    if (checks >= 3) score++;
    if (checks >= 4) score++;

    var cfg = strengthConfig[score] || strengthConfig[0];
    bar.style.width      = cfg.pct + '%';
    bar.style.background = cfg.color;
    text.textContent     = cfg.label;
    text.style.color     = cfg.color;
}

// ── SLIDER ────────────────────────────────────────────────────────────────────
var sliderProps = {
    fill:       '#1c71d8',
    background: 'rgba(28,113,216,.15)',
};

var sliderContainer = document.querySelector('.range__slider');
var sliderValue     = document.querySelector('.length__title');

function applyFill(sliderEl) {
    var percentage = (100 * (sliderEl.value - sliderEl.min)) / (sliderEl.max - sliderEl.min);
    sliderEl.style.background =
        'linear-gradient(90deg, ' + sliderProps.fill + ' ' + percentage + '%, ' +
        sliderProps.background + ' ' + (percentage + 0.1) + '%)';
    if (sliderValue) sliderValue.setAttribute('data-length', sliderEl.value);
    var badge = document.getElementById('pgSizeBadge');
    if (badge) badge.textContent = sliderEl.value;
    updateStrength();
}

if (sliderContainer) {
    var rangeInput = sliderContainer.querySelector('input');
    if (rangeInput) {
        rangeInput.addEventListener('input', function(e) { applyFill(e.target); });
        applyFill(rangeInput);
    }
}

// ── RANDOM CHAR FUNCTIONS ─────────────────────────────────────────────────────
var randomFunc = {
    lower:  getRandomLower,
    upper:  getRandomUpper,
    number: getRandomNumber,
    symbol: getRandomSymbol,
};

function secureMathRandom() {
    return window.crypto.getRandomValues(new Uint32Array(1))[0] / (Math.pow(2, 32) - 1);
}
function getRandomLower()  { return String.fromCharCode(Math.floor(Math.random() * 26) + 97); }
function getRandomUpper()  { return String.fromCharCode(Math.floor(Math.random() * 26) + 65); }
function getRandomNumber() { return String.fromCharCode(Math.floor(secureMathRandom() * 10) + 48); }
function getRandomSymbol() {
    var symbols = '~!@#$%^&*()_+{}":?><;.,';
    return symbols[Math.floor(Math.random() * symbols.length)];
}

// ── DOM REFS ──────────────────────────────────────────────────────────────────
var resultEl        = document.getElementById('result');
var lengthEl        = document.getElementById('slider');
var uppercaseEl     = document.getElementById('uppercase');
var lowercaseEl     = document.getElementById('lowercase');
var numberEl        = document.getElementById('number');
var symbolEl        = document.getElementById('symbol');
var generateBtn     = document.getElementById('generate');
var copyBtn         = document.getElementById('copy-btn');
var resultContainer = document.querySelector('.result');
var copyInfo        = document.querySelector('.result__info.right');
var copiedInfo      = document.querySelector('.result__info.left');
var editPasswordTarget = document.getElementById('senhamodal');

var generatedPassword = false;

// ── COPY CURSOR (segue o mouse) ───────────────────────────────────────────────
var resultContainerBound = resultContainer ? resultContainer.getBoundingClientRect() : {};

if (resultContainer) {
    resultContainer.addEventListener('mousemove', function(e) {
        resultContainerBound = resultContainer.getBoundingClientRect();
        if (generatedPassword) {
            copyBtn.style.opacity = '1';
            copyBtn.style.pointerEvents = 'all';
            copyBtn.style.setProperty('--x', (e.x - resultContainerBound.left) + 'px');
            copyBtn.style.setProperty('--y', (e.y - resultContainerBound.top) + 'px');
        } else {
            copyBtn.style.opacity = '0';
            copyBtn.style.pointerEvents = 'none';
        }
    });
}

window.addEventListener('resize', function() {
    if (resultContainer) resultContainerBound = resultContainer.getBoundingClientRect();
});

// ── COPY ──────────────────────────────────────────────────────────────────────
if (copyBtn) {
    copyBtn.addEventListener('click', function() {
        var password = resultEl ? resultEl.innerText : '';
        if (!password || password === 'CLIQUE EM GERAR') return;

        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(password).catch(function() { fallbackCopy(password); });
        } else {
            fallbackCopy(password);
        }

        if (copyInfo)   { copyInfo.style.transform  = 'translateY(200%)'; copyInfo.style.opacity  = '0'; }
        if (copiedInfo) { copiedInfo.style.transform = 'translateY(0%)';   copiedInfo.style.opacity = '0.9'; }
    });
}

function fallbackCopy(text) {
    var ta = document.createElement('textarea');
    ta.value = text;
    document.body.appendChild(ta);
    ta.select();
    document.execCommand('copy');
    ta.remove();
}

// ── GENERATE ──────────────────────────────────────────────────────────────────
if (generateBtn) {
    generateBtn.addEventListener('click', function() {
        var length    = lengthEl    ? +lengthEl.value       : 16;
        var hasLower  = lowercaseEl ? lowercaseEl.checked   : false;
        var hasUpper  = uppercaseEl ? uppercaseEl.checked   : false;
        var hasNumber = numberEl    ? numberEl.checked      : false;
        var hasSymbol = symbolEl    ? symbolEl.checked      : false;

        generatedPassword = true;
        var generated = generatePassword(length, hasLower, hasUpper, hasNumber, hasSymbol);
        if (resultEl) resultEl.innerText = generated;
        if (editPasswordTarget && generated) {
            editPasswordTarget.value = generated;
        }
        if (copyInfo)   { copyInfo.style.transform  = 'translateY(0%)';   copyInfo.style.opacity  = '0.75'; }
        if (copiedInfo) { copiedInfo.style.transform = 'translateY(200%)'; copiedInfo.style.opacity = '0'; }
    });
}

function generatePassword(length, lower, upper, number, symbol) {
    var pwd = '';
    var typesCount = lower + upper + number + symbol;
    var typesArr = [
        { lower: lower }, { upper: upper }, { number: number }, { symbol: symbol }
    ].filter(function(item) { return Object.values(item)[0]; });

    if (typesCount === 0) return '';

    for (var i = 0; i < length; i++) {
        typesArr.forEach(function(type) {
            pwd += randomFunc[Object.keys(type)[0]]();
        });
    }
    return pwd.slice(0, length).split('').sort(function() { return Math.random() - 0.5; }).join('');
}

// ── DISABLE ÚLTIMO CHECKBOX ───────────────────────────────────────────────────
function disableOnlyCheckbox() {
    var all     = [uppercaseEl, lowercaseEl, numberEl, symbolEl].filter(Boolean);
    var checked = all.filter(function(el) { return el.checked; });
    all.forEach(function(el) {
        el.disabled = (checked.length === 1 && el.checked);
    });
    updateStrength();
}

[uppercaseEl, lowercaseEl, numberEl, symbolEl].forEach(function(el) {
    if (el) el.addEventListener('click', disableOnlyCheckbox);
});

// Estado inicial da barra de força
updateStrength();
