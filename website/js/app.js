// Γράφει το τρέχον έτος στο υποσέλιδο.
document.addEventListener('DOMContentLoaded', function () {
  var year = document.getElementById('year');
  if (year) {
    year.textContent = String(new Date().getFullYear());
  }
});
