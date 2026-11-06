document.addEventListener('DOMContentLoaded', function() {
    // Funcionalidad para el menú lateral (sidebar)
    const sidebar = document.getElementById('sidebar');
    const toggleBtn = document.getElementById('toggleBtn');

    toggleBtn.addEventListener('click', () => {
        sidebar.classList.toggle('collapsed');
    });
    
    // 1. Gráfico de Líneas: Préstamos Mensuales
    const ctxPrestamos = document.getElementById('chartPrestamos').getContext('2d');
    new Chart(ctxPrestamos, {
        type: 'line',
        data: {
            labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago'],
            datasets: [{
                label: 'Préstamos 2026',
                data: [42, 58, 75, 63, 90, 84, 112, 100],
                borderColor: '#2563eb',
                backgroundColor: 'rgba(37, 99, 235, 0.08)',
                borderWidth: 2.5,
                fill: true,
                tension: 0.35
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { grid: { color: '#f1f5f9' }, ticks: { color: '#94a3b8', font: { size: 10 } } },
                x: { grid: { display: false }, ticks: { color: '#94a3b8', font: { size: 10 } } }
            }
        }
    });

    // 2. Gráfico de Barras Horizontales: Top 5 Libros Más Pedidos
    const ctxTopLibros = document.getElementById('chartTopLibros').getContext('2d');
    new Chart(ctxTopLibros, {
        type: 'bar',
        data: {
            labels: ['Cálculo', 'Física Univ.', 'Algoritmos', 'Química Gen.', 'Hist. Arg.'],
            datasets: [{
                data: [84, 62, 55, 48, 39],
                backgroundColor: '#38bdf8',
                borderRadius: 6
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { color: '#f1f5f9' }, ticks: { color: '#94a3b8', font: { size: 10 } } },
                y: { grid: { display: false }, ticks: { color: '#475569', font: { size: 11 } } }
            }
        }
    });

    // 3. Gráfico Donut: Distribución por Categoría CDU
    const ctxCategorias = document.getElementById('chartCategorias').getContext('2d');
    new Chart(ctxCategorias, {
        type: 'doughnut',
        data: {
            labels: ['Ciencias', 'Tecnología', 'Matemáticas', 'Humanidades'],
            datasets: [{
                data: [312, 245, 198, 167],
                backgroundColor: ['#2563eb', '#38bdf8', '#1e3a8a', '#93c5fd']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { boxWidth: 10, font: { size: 10 }, padding: 8 }
                }
            }
        }
    });

});