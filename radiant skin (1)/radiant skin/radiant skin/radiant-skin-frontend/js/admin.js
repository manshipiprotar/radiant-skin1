/* ===========================================================
   RADIANT SKIN — Admin Panel Shared Layout
   Renders the sidebar + topbar used on every admin-*.html page.
   =========================================================== */

function rsAdminSidebar(active){
  const items = [
    { key: "dashboard", href: "admin-dashboard.html", icon: "bi-grid", label: "Dashboard" },
    { key: "products",  href: "admin-products.html",  icon: "bi-bag", label: "Manage Products" },
    { key: "orders",    href: "admin-orders.html",     icon: "bi-receipt", label: "Manage Orders" },
    { key: "users",     href: "admin-users.html",      icon: "bi-people", label: "Manage Users" },
    { key: "messages",  href: "admin-messages.html",   icon: "bi-chat-dots", label: "Customer Messages" },
    { key: "reviews",   href: "admin-reviews.html",    icon: "bi-star", label: "Product Reviews" },
    { key: "reports",   href: "admin-reports.html",    icon: "bi-graph-up-arrow", label: "Sales & Analytics" }
  ];
  const linksHtml = items.map(i => `
    <a href="${i.href}" class="admin-nav-link ${active===i.key ? 'active' : ''}">
      <i class="bi ${i.icon}"></i> ${i.label}
    </a>`).join("");

  return `
  <aside class="admin-sidebar">
    <div class="admin-brand"><i class="bi bi-flower1"></i> Radiant Skin <span class="small">Admin</span></div>
    <nav class="admin-nav">${linksHtml}</nav>
    <button class="admin-logout-btn" onclick="rsAdminLogout()"><i class="bi bi-box-arrow-left"></i> Logout</button>
  </aside>`;
}

function rsMountAdmin(active){
  rsRequireAdmin();
  const el = document.getElementById("rs-admin-sidebar");
  if(el) el.innerHTML = rsAdminSidebar(active);
}