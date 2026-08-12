// navbar.js — inicialização reutilizável da navbar e slim topbar

// Preenche nome e iniciais do usuário nos dois modos de navegação:
//   • navbar topo  → #navNome (primeiro nome) + #userInitials (2 letras)
//   • slim topbar  → #navNomeSlim (nome completo) + #userInitialsSlim (1 letra)
//
// Se o elemento de avatar já contiver uma <img> (foto carregada via carregarFoto),
// as iniciais NÃO sobrescrevem — evita race condition entre navbarInit e carregarFoto.
window.navbarInit = function(nome) {
    var nomeCompleto = nome || '';
    var primeiroNome = nomeCompleto.split(' ')[0] || nomeCompleto;

    // Navbar topo
    var elNome = document.getElementById('navNome');
    var elIni  = document.getElementById('userInitials');
    if (elNome) elNome.textContent = primeiroNome;
    if (elIni && !elIni.querySelector('img')) {
        elIni.textContent = (nomeCompleto || '?').split(' ')
            .slice(0, 2).map(function(p) { return p.charAt(0); })
            .join('').toUpperCase();
    }

    // Slim topbar (modo sidebar)
    var elNomeSlim = document.getElementById('navNomeSlim');
    var elIniSlim  = document.getElementById('userInitialsSlim');
    if (elNomeSlim) elNomeSlim.textContent = nomeCompleto;
    if (elIniSlim && !elIniSlim.querySelector('img')) {
        elIniSlim.textContent = (nomeCompleto || '?').charAt(0).toUpperCase();
    }

    document.querySelectorAll('.user-btn').forEach(function(btn) { btn.classList.add('nav-loaded'); });
};


function _aplicarBadgeEmail(id, qtd) {
    var badge = document.getElementById(id);
    if (!badge) return;
    badge.textContent = qtd;
    if (qtd > 0) {
        badge.style.display = 'inline-flex';
        badge.classList.add('email-alerta');
    } else {
        badge.style.display = 'none';
        badge.classList.remove('email-alerta');
    }
}

// Auto-executa em todas as páginas via endpoint leve (sem carregar dados completos da home)
document.addEventListener('DOMContentLoaded', function() {
    fetch('/judi/funcionarios/nav')
        .then(function(r) { return r.ok ? r.json() : null; })
        .then(function(data) {
            if (data && data.nomeFuncionario) window.navbarInit(data.nomeFuncionario);
        })
        .catch(function() {});
});