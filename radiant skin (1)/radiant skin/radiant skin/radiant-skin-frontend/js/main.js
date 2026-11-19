/* =====================================================================
   RADIANT SKIN — main.js
   Simple JavaScript for cart, wishlist, login and reviews.
   Everything is stored in the browser's localStorage (no real backend).
   ===================================================================== */

/* ---------------------------------------------------------------
   1. BASIC STORAGE HELPERS
   localStorage can only store text, so we convert objects to text
   with JSON.stringify() when saving, and back with JSON.parse()
   when reading.
   --------------------------------------------------------------- */
function rsGet(key, defaultValue) {
  var text = localStorage.getItem(key);
  if (text === null) return defaultValue;
  return JSON.parse(text);
}

function rsSet(key, value) {
  localStorage.setItem(key, JSON.stringify(value));
}


/* ---------------------------------------------------------------
   2. CART
   Cart is stored as an array like:
   [ { id: "P001", qty: 2 }, { id: "P011", qty: 1 } ]
   --------------------------------------------------------------- */
function rsGetCart() {
  return rsGet("rs_cart", []);
}

function rsSaveCart(cart) {
  rsSet("rs_cart", cart);
  if (typeof rsRefreshNavbar === "function") {
    rsRefreshNavbar();
  }
}

function rsAddToCart(productId, qty) {
  var cart = rsGetCart();
  var existing = null;
  for (var i = 0; i < cart.length; i++) {
    if (cart[i].id === productId) existing = cart[i];
  }
  if (existing) {
    existing.qty = existing.qty + qty;
  } else {
    cart.push({ id: productId, qty: qty });
  }
  rsSaveCart(cart);
}

function rsRemoveFromCart(productId) {
  var cart = rsGetCart();
  var newCart = [];
  for (var i = 0; i < cart.length; i++) {
    if (cart[i].id !== productId) newCart.push(cart[i]);
  }
  rsSaveCart(newCart);
}

function rsUpdateCartQty(productId, newQty) {
  var cart = rsGetCart();
  for (var i = 0; i < cart.length; i++) {
    if (String(cart[i].id) === String(productId)) cart[i].qty = Number(newQty);
  }
  rsSaveCart(cart);
}

function rsSetCartQty(productId, newQty) {
  rsUpdateCartQty(productId, newQty);
}

function rsCartCount() {
  var cart = rsGetCart();
  var total = 0;
  for (var i = 0; i < cart.length; i++) total += cart[i].qty;
  return total;
}

function rsCartTotal() {
  var cart = rsGetCart();
  var total = 0;
  for (var i = 0; i < cart.length; i++) {
    var product = rsFindProduct(cart[i].id);
    if (product) total += product.price * cart[i].qty;
  }
  return total;
}


/* ---------------------------------------------------------------
   3. WISHLIST
   Wishlist is just an array of product ids, e.g. ["P001", "P015"]
   --------------------------------------------------------------- */
function rsGetWishlist() {
  return rsGet("rs_wishlist", []);
}

function rsIsWishlisted(productId) {
  return rsGetWishlist().indexOf(productId) !== -1;
}

function rsToggleWishlist(productId) {
  var list = rsGetWishlist();
  var index = list.indexOf(productId);
  if (index === -1) {
    list.push(productId);
  } else {
    list.splice(index, 1);
  }
  rsSet("rs_wishlist", list);
  if (typeof rsRefreshNavbar === "function") {
    rsRefreshNavbar();
  }
  return rsIsWishlisted(productId);
}


/* ---------------------------------------------------------------
   4. LOGIN / REGISTER
   All registered users are stored in "rs_users".
   The currently logged-in user is stored separately in "rs_user".
   --------------------------------------------------------------- */
function rsGetUsers() {
  return rsGet("rs_users", []);
}

function rsRegisterUser(user) {
  var users = rsGetUsers();
  users.push(user);
  rsSet("rs_users", users);
}

function rsLogin(email, password) {
  var users = rsGetUsers();
  for (var i = 0; i < users.length; i++) {
    if (users[i].email.toLowerCase() === email.toLowerCase() && users[i].password === password) {
      rsSet("rs_user", {
        name: users[i].name,
        email: users[i].email,
        phone: users[i].phone || '',
        city: users[i].city || ''
      });
      return true;
    }
  }
  return false;
}

function rsUpdateCurrentUser(updatedFields) {
  var current = rsCurrentUser();
  if (!current) return null;
  var user = Object.assign({}, current, updatedFields);
  rsSet("rs_user", user);

  // Also update in all users array "rs_users"
  var users = rsGetUsers();
  for (var i = 0; i < users.length; i++) {
    if (users[i].email.toLowerCase() === user.email.toLowerCase()) {
      users[i] = Object.assign({}, users[i], updatedFields);
    }
  }
  rsSet("rs_users", users);

  if (typeof rsRefreshNavbar === "function") {
    rsRefreshNavbar();
  }
  return user;
}

function rsLogout() {
  localStorage.removeItem("rs_user");
  window.location.href = "index.html";
}

function rsCurrentUser() {
  return rsGet("rs_user", null);
}

function rsIsLoggedIn() {
  return rsCurrentUser() !== null;
}

/* ---------------------------------------------------------------
   ADMIN AUTH (simple, hardcoded demo credentials for the college project)
   Email: admin@radiantskin.com   Password: admin123
   --------------------------------------------------------------- */
function rsAdminLogin(email, password) {
  if (email === "admin@radiantskin.com" && password === "admin123") {
    rsSet("rs_admin", true);
    return true;
  }
  return false;
}

function rsIsAdminLoggedIn() {
  return rsGet("rs_admin", false) === true;
}

function rsAdminLogout() {
  localStorage.removeItem("rs_admin");
  window.location.href = "admin-login.html";
}

/* Call this at the top of every admin page to block non-admin visitors */
function rsRequireAdmin() {
  if (!rsIsAdminLoggedIn()) {
    window.location.href = "admin-login.html";
  }
}

/* Simple login gate — used before letting someone add to cart,
   write a review, etc. If not logged in, it just sends the
   person to the login page. Nothing fancy is auto-replayed —
   they simply click the button again after logging in. */
function rsRequireLogin() {
  if (rsIsLoggedIn()) return true;
  sessionStorage.setItem("rs_redirect", window.location.pathname.split("/").pop());
  rsToast("Please login or register first.", "info");
  setTimeout(function () {
    window.location.href = "login.html";
  }, 800);
  return false;
}

function rsAddToCartGated(productId, qty) {
  if (!rsRequireLogin()) return;
  rsAddToCart(productId, qty || 1);
  rsToast("Added to your bag ✓", "success");
}


/* ---------------------------------------------------------------
   5. PRODUCT REVIEWS
   Reviews for each product are stored under "rs_reviews" like:
   { "P001": [ { name, rating, text, own }, ... ] }
   --------------------------------------------------------------- */
function rsGetReviews(productId) {
  var all = rsGet("rs_reviews", {});
  return all[productId] || [];
}

function rsSaveReviews(productId, list) {
  var all = rsGet("rs_reviews", {});
  all[productId] = list;
  rsSet("rs_reviews", all);
}

function rsAddReview(productId, review) {
  var list = rsGetReviews(productId);
  review.id = "r" + Date.now();
  list.unshift(review);
  rsSaveReviews(productId, list);
}

function rsDeleteReview(productId, reviewId) {
  var list = rsGetReviews(productId);
  var newList = [];
  for (var i = 0; i < list.length; i++) {
    if (list[i].id !== reviewId) newList.push(list[i]);
  }
  rsSaveReviews(productId, newList);
}

function rsGetProductRatingInfo(productId) {
  var reviews = rsGetReviews(productId);
  if (!reviews || reviews.length === 0) {
    return { rating: 1.0, count: 0 };
  }
  var sum = 0;
  for (var i = 0; i < reviews.length; i++) {
    sum += Number(reviews[i].rating) || 1;
  }
  var avg = (sum / reviews.length).toFixed(1);
  return { rating: Number(avg), count: reviews.length };
}

/* Simple average of whatever reviews are currently stored (defaults to 1.0) */
function rsAverageRating(productId, fallbackRating) {
  return rsGetProductRatingInfo(productId).rating;
}

function rsReviewCount(productId) {
  return rsGetProductRatingInfo(productId).count;
}


/* ---------------------------------------------------------------
   6. SMALL UI HELPERS
   --------------------------------------------------------------- */

/* Format a number as Indian Rupees, e.g. 1234 -> "₹1,234" */
function rsFormatINR(amount) {
  return "₹" + Math.round(amount).toLocaleString("en-IN");
}

/* Turn a rating number into filled/empty star icons (defaults to 1 star if unrated) */
function rsStars(rating) {
  var r = Number(rating);
  if (isNaN(r) || r < 1) r = 1;
  var fullStars = Math.round(r);
  if (fullStars > 5) fullStars = 5;
  var html = "";
  for (var i = 1; i <= 5; i++) {
    html += i <= fullStars ? '<i class="bi bi-star-fill text-warning"></i>' : '<i class="bi bi-star text-muted opacity-50"></i>';
  }
  return html;
}

/* Show a small message box in the top-right corner for 2.5 seconds */
function rsToast(message, type) {
  var wrap = document.getElementById("rsToastWrap");
  if (!wrap) {
    wrap = document.createElement("div");
    wrap.id = "rsToastWrap";
    wrap.style.position = "fixed";
    wrap.style.top = "20px";
    wrap.style.right = "20px";
    wrap.style.zIndex = "9999";
    document.body.appendChild(wrap);
  }
  var box = document.createElement("div");
  box.textContent = message;
  box.style.background = type === "danger" ? "#C6707A" : (type === "info" ? "#7C5B99" : "#4B3438");
  box.style.color = "#fff";
  box.style.padding = "12px 18px";
  box.style.borderRadius = "10px";
  box.style.marginTop = "8px";
  box.style.fontSize = "14px";
  box.style.boxShadow = "0 6px 16px rgba(0,0,0,0.15)";
  wrap.appendChild(box);
  setTimeout(function () { box.remove(); }, 2500);
}

/* Simple hero image slider: shows one slide at a time, auto-advances every 2s */
function rsInitSlider(rootId, intervalMs) {
  var root = document.getElementById(rootId);
  if (!root) return;
  var slides = root.querySelectorAll(".hero-slide");
  var dots = root.querySelectorAll(".hero-dots button");
  var current = 0;

  function showSlide(index) {
    for (var i = 0; i < slides.length; i++) slides[i].classList.toggle("active", i === index);
    for (var i = 0; i < dots.length; i++) dots[i].classList.toggle("active", i === index);
    current = index;
  }

  for (var i = 0; i < dots.length; i++) {
    dots[i].addEventListener("click", (function (index) {
      return function () { showSlide(index); };
    })(i));
  }

  setInterval(function () {
    showSlide((current + 1) % slides.length);
  }, intervalMs || 2000);
}