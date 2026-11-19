/* ===========================================================
   RADIANT SKIN — Shared Navbar / Footer Partials
   Injected into <div id="rs-navbar"></div> / <div id="rs-footer"></div>
   =========================================================== */

const RS_LOGO_IMG = `<img src="images/logo.jpg" alt="Radiant Skin Studio" style="height:42px; width:auto; object-fit:contain; border-radius:6px; background:#fff; padding:2px;">`;

function rsRenderNavbar(active){
  const links = [
    { href: "index.html", label: "Home", key: "home" },
    { href: "shop.html", label: "Shop", key: "shop" },
    { href: "about.html", label: "About", key: "about" },
    { href: "contact.html", label: "Contact", key: "contact" }
  ];
  const linksHtml = links.map(l => `<a class="nav-link ${active===l.key?'active':''}" href="${l.href}">${l.label}</a>`).join("");

  return `
  <nav class="rs-navbar sticky-top">
    <div class="container d-flex align-items-center justify-content-between">
      <a href="index.html" class="rs-logo d-inline-flex align-items-center gap-2">${RS_LOGO_IMG}<span>Radiant Skin</span></a>
      <div class="d-none d-lg-flex align-items-center">${linksHtml}</div>
      <div class="d-flex align-items-center gap-2">
        <a href="wishlist.html" class="rs-icon-btn" title="Wishlist">
          <i class="bi bi-heart"></i>
          <span class="rs-badge-count js-wishlist-count">0</span>
        </a>
        <a href="cart.html" class="rs-icon-btn" title="Cart">
          <i class="bi bi-bag"></i>
          <span class="rs-badge-count js-cart-count">0</span>
        </a>
        <div class="dropdown">
          <button class="rs-icon-btn border-0" data-bs-toggle="dropdown" title="Account">
            <i class="bi bi-person"></i>
          </button>
          <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 mt-2" style="border-radius:14px;">
            <li class="js-if-logged-in" style="display:none;"><span class="dropdown-item-text small text-muted">Hi, <span class="js-user-name">there</span> 👋</span></li>
            <li class="js-if-logged-in" style="display:none;"><a class="dropdown-item" href="profile.html"><i class="bi bi-person-circle me-2"></i>My Profile</a></li>
            <li class="js-if-logged-in" style="display:none;"><a class="dropdown-item" href="order-history.html"><i class="bi bi-bag-check me-2"></i>My Orders</a></li>
            <li class="js-if-logged-in" style="display:none;"><a class="dropdown-item" href="wishlist.html"><i class="bi bi-heart me-2"></i>My Wishlist</a></li>
            <li class="js-if-logged-in" style="display:none;"><hr class="dropdown-divider"></li>
            <li class="js-if-logged-in" style="display:none;"><a class="dropdown-item" href="#" onclick="rsLogout();return false;"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
            <li class="js-if-logged-out"><a class="dropdown-item" href="login.html"><i class="bi bi-box-arrow-in-right me-2"></i>Login</a></li>
            <li class="js-if-logged-out"><a class="dropdown-item" href="login.html?tab=register"><i class="bi bi-person-plus me-2"></i>Register</a></li>
            <li class="js-if-logged-out"><a class="dropdown-item" href="forgot-password.html"><i class="bi bi-key me-2"></i>Forgot Password</a></li>
          </ul>
        </div>
        <button class="rs-icon-btn border-0 d-lg-none" data-bs-toggle="offcanvas" data-bs-target="#rsMobileNav"><i class="bi bi-list"></i></button>
      </div>
    </div>
  </nav>
  <div class="offcanvas offcanvas-end" tabindex="-1" id="rsMobileNav" style="background:var(--cream);">
    <div class="offcanvas-header">
      <span class="rs-logo d-inline-flex align-items-center gap-2">${RS_LOGO_IMG}<span>Radiant Skin</span></span>
      <button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button>
    </div>
    <div class="offcanvas-body d-flex flex-column gap-3 fs-5">${links.map(l => `<a class="nav-link ${active===l.key?'active':''}" href="${l.href}">${l.label}</a>`).join("")}</div>
  </div>`;
}

function rsRenderFooter(){
  return `
  <footer class="rs-footer">
    <div class="container">
      <div class="row g-4">
        <div class="col-lg-4 col-md-6">
          <a href="index.html" class="rs-logo mb-3 d-inline-flex align-items-center gap-2" style="color:#fff;">${RS_LOGO_IMG}<span>Radiant Skin</span></a>
          <p class="small mb-3" style="color:#D9C4C9;">Restoring your skin's natural luminosity through minimal, sustainable botanical skincare rituals.</p>
          <div>
            <a href="#" class="social-icon"><i class="bi bi-instagram"></i></a>
            <a href="#" class="social-icon"><i class="bi bi-facebook"></i></a>
            <a href="#" class="social-icon"><i class="bi bi-pinterest"></i></a>
            <a href="#" class="social-icon"><i class="bi bi-twitter-x"></i></a>
          </div>
        </div>
        <div class="col-lg-2 col-md-6 col-6">
          <h6>Quick Links</h6>
          <a href="index.html">Home</a>
          <a href="shop.html">Shop All</a>
          <a href="about.html">About Us</a>
          <a href="contact.html">Contact</a>
        </div>
        <div class="col-lg-3 col-md-6 col-6">
          <h6>Categories</h6>
          <a href="shop.html?cat=serum">Serum</a>
          <a href="shop.html?cat=cleanser">Face Wash / Cleanser</a>
          <a href="shop.html?cat=moisturizer">Moisturizer</a>
          <a href="shop.html?cat=sunscreen">Sunscreen</a>
          <a href="shop.html?cat=nightcream">Night Cream</a>
          <a href="shop.html?cat=toner">Toner</a>
        </div>
        <div class="col-lg-3 col-md-6">
          <h6>Contact Us</h6>
          <a href="#"><i class="bi bi-geo-alt me-1"></i> Rajkot, Gujarat, India</a>
          <a href="mailto:hello@radiantskin.com"><i class="bi bi-envelope me-1"></i> hello@radiantskin.com</a>
          <a href="tel:+911234567890"><i class="bi bi-telephone me-1"></i> +91 12345 67890</a>
        </div>
      </div>
      <hr>
      <div class="d-flex flex-wrap justify-content-between align-items-center footer-bottom">
        <span>&copy; 2026 Radiant Skin. All rights reserved. | BCA Academic Project</span>
        <span>Made with <i class="bi bi-heart-fill"></i> for radiant skin</span>
      </div>
    </div>
  </footer>`;
}

function rsMountPartials(activePage){
  const navEl = document.getElementById("rs-navbar");
  const footEl = document.getElementById("rs-footer");
  if(navEl) navEl.innerHTML = rsRenderNavbar(activePage);
  if(footEl) footEl.innerHTML = rsRenderFooter();
  rsRefreshNavbar();
}

/* Shows/hides the login-only and logged-out-only menu items,
   fills in the user's name, and updates the cart/wishlist badges. */
function rsRefreshNavbar(){
  var user = rsCurrentUser();

  var loggedInItems = document.querySelectorAll(".js-if-logged-in");
  var loggedOutItems = document.querySelectorAll(".js-if-logged-out");
  for(var i = 0; i < loggedInItems.length; i++) loggedInItems[i].style.display = user ? "" : "none";
  for(var i = 0; i < loggedOutItems.length; i++) loggedOutItems[i].style.display = user ? "none" : "";

  var nameSpans = document.querySelectorAll(".js-user-name");
  for(var i = 0; i < nameSpans.length; i++) nameSpans[i].textContent = user ? user.name : "there";

  var cartCount = rsCartCount();
  var cartBadges = document.querySelectorAll(".js-cart-count");
  for(var i = 0; i < cartBadges.length; i++) {
    var b = cartBadges[i];
    if(b.textContent !== String(cartCount)){
      b.textContent = cartCount;
      b.style.transform = "scale(1.35)";
      b.style.transition = "transform 0.2s ease";
      (function(badge){
        setTimeout(function(){ badge.style.transform = "scale(1)"; }, 200);
      })(b);
    } else {
      b.textContent = cartCount;
    }
  }

  var wishCount = rsGetWishlist().length;
  var wishBadges = document.querySelectorAll(".js-wishlist-count");
  for(var i = 0; i < wishBadges.length; i++) {
    var wb = wishBadges[i];
    if(wb.textContent !== String(wishCount)){
      wb.textContent = wishCount;
      wb.style.transform = "scale(1.35)";
      wb.style.transition = "transform 0.2s ease";
      (function(badge){
        setTimeout(function(){ badge.style.transform = "scale(1)"; }, 200);
      })(wb);
    } else {
      wb.textContent = wishCount;
    }
  }
}