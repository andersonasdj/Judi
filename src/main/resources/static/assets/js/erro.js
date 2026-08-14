(function () {
	if (localStorage.getItem('darkMode') === 'enabled') {
		document.body.classList.add('dark-mode');
	}

	document.addEventListener('DOMContentLoaded', function () {
		var btn = document.getElementById('toggleDarkBtn');
		if (!btn) return;

		atualizarIcone();
		btn.addEventListener('click', function () {
			document.body.classList.toggle('dark-mode');
			var ativo = document.body.classList.contains('dark-mode');
			localStorage.setItem('darkMode', ativo ? 'enabled' : 'disabled');
			atualizarIcone();
		});

		function atualizarIcone() {
			btn.innerHTML = document.body.classList.contains('dark-mode')
				? '<i class="bi bi-sun"></i>'
				: '<i class="bi bi-moon"></i>';
		}
	});
})();
