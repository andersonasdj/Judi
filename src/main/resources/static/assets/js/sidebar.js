// sidebar.js — funções reutilizáveis de layout, sidebar e flyout
// Incluir após bootstrap.bundle.min.js em qualquer página que use a sidebar/navbar padrão.

function toggleLayout() {
    const next = (document.body.dataset.layout || 'top') === 'top' ? 'side' : 'top';
    document.body.dataset.layout = next;
    localStorage.setItem('navLayout', next);
    const nav = document.getElementById('sidebarNav');
    const overlay = document.getElementById('sidebarOverlay');
    if (nav) nav.classList.remove('mobile-open');
    if (overlay) overlay.classList.remove('active');
}

function toggleSidebarCollapse() {
    const collapsed = document.body.classList.toggle('sidebar-collapsed');
    localStorage.setItem('sidebarCollapsed', collapsed);
    const icon = document.getElementById('sidebarCollapseIcon');
    if (icon) icon.className = collapsed ? 'bi bi-arrow-bar-right' : 'bi bi-arrow-bar-left';
    if (!collapsed) closeSidebarFlyout();
}

function toggleSidebarGroup(id) {
    if (document.body.classList.contains('sidebar-collapsed')) {
        showSidebarFlyout(id);
        return;
    }
    document.getElementById(id)?.classList.toggle('open');
}

function showSidebarFlyout(groupId) {
    const group = document.getElementById(groupId);
    if (!group) return;
    const flyout = document.getElementById('sidebarFlyout');
    if (!flyout) return;

    if (flyout.dataset.activeGroup === groupId && flyout.classList.contains('active')) {
        closeSidebarFlyout();
        return;
    }

    const header = group.querySelector('.sidebar-group-header');
    const itemsEl = group.querySelector('.sidebar-group-items');
    const titleEl = document.getElementById('sidebarFlyoutTitle');
    const itemsOut = document.getElementById('sidebarFlyoutItems');
    if (titleEl) titleEl.textContent = header?.querySelector('.sidebar-group-label')?.textContent?.trim() || '';
    if (itemsOut) itemsOut.innerHTML = itemsEl?.innerHTML || '';

    const rect = group.getBoundingClientRect();
    flyout.style.top = rect.top + 'px';
    flyout.dataset.activeGroup = groupId;
    flyout.classList.add('active');

    requestAnimationFrame(() => {
        const fRect = flyout.getBoundingClientRect();
        if (fRect.bottom > window.innerHeight - 8) {
            flyout.style.top = (parseFloat(flyout.style.top) - (fRect.bottom - window.innerHeight + 8)) + 'px';
        }
    });
}

function closeSidebarFlyout() {
    const flyout = document.getElementById('sidebarFlyout');
    if (flyout) { flyout.classList.remove('active'); flyout.dataset.activeGroup = ''; }
}

function openSidebarMobile() {
    document.getElementById('sidebarNav')?.classList.add('mobile-open');
    document.getElementById('sidebarOverlay')?.classList.add('active');
}

function closeSidebarMobile() {
    document.getElementById('sidebarNav')?.classList.remove('mobile-open');
    document.getElementById('sidebarOverlay')?.classList.remove('active');
}

function iniciarLayout() {
    if (localStorage.getItem('sidebarCollapsed') === 'true') {
        document.body.classList.add('sidebar-collapsed');
        const icon = document.getElementById('sidebarCollapseIcon');
        if (icon) icon.className = 'bi bi-arrow-bar-right';
    }
}

// Fecha flyout ao clicar fora da sidebar
document.addEventListener('click', (e) => {
    const flyout = document.getElementById('sidebarFlyout');
    if (flyout?.classList.contains('active')) {
        const nav = document.getElementById('sidebarNav');
        if (!flyout.contains(e.target) && !nav?.contains(e.target)) {
            closeSidebarFlyout();
        }
    }
});

// Inicializa layout ao carregar
document.addEventListener('DOMContentLoaded', iniciarLayout);
