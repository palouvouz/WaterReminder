// Διαβάζει τα δεδομένα από το hash του URL (#drunk=500&goal=2000) και ζωγραφίζει τον κύκλο.
// Το Kotlin αλλάζει μόνο το URL, οπότε δεν υπάρχει JavaScript μέσα σε αρχεία Kotlin.
(function () {
  var RADIUS = 92;
  var CIRCUMFERENCE = 2 * Math.PI * RADIUS;

  function readParams() {
    var params = { drunk: 0, goal: 0 };
    location.hash.replace(/^#/, '').split('&').forEach(function (pair) {
      var parts = pair.split('=');
      if (parts[0] === 'drunk' || parts[0] === 'goal') {
        var value = parseInt(parts[1], 10);
        params[parts[0]] = isNaN(value) || value < 0 ? 0 : value;
      }
    });
    return params;
  }

  function render() {
    var p = readParams();
    var fraction = p.goal > 0 ? Math.min(1, p.drunk / p.goal) : 0;
    var bar = document.getElementById('ring-bar');
    bar.style.strokeDasharray = String(CIRCUMFERENCE);
    bar.style.strokeDashoffset = String(CIRCUMFERENCE * (1 - fraction));
    document.getElementById('ring-percent').textContent = Math.round(fraction * 100) + '%';
    document.getElementById('ring-amount').textContent =
      (p.drunk / 1000).toFixed(2) + ' / ' + (p.goal / 1000).toFixed(2) + ' L';
  }

  document.addEventListener('DOMContentLoaded', function () {
    var bar = document.getElementById('ring-bar');
    bar.style.strokeDasharray = String(CIRCUMFERENCE);
    bar.style.strokeDashoffset = String(CIRCUMFERENCE);
    // Ένα frame αργότερα, ώστε να παίξει το transition από το 0.
    window.requestAnimationFrame(render);
  });
  window.addEventListener('hashchange', render);
})();
