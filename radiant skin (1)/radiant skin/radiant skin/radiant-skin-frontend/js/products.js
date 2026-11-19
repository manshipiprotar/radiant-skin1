/* ===========================================================
   RADIANT SKIN — Dynamic Product & Category Data
   Driven strictly by MySQL Database & Admin Panel Operations
   =========================================================== */

const RS_DEFAULT_CATEGORIES = {};
const RS_DEFAULT_PRODUCTS = [];

// ---------- Fallback illustration (shown automatically if a real photo is missing) ----------
function rsProductSVG(catKey, seed) {
  const cats = (typeof rsGetCategories === 'function') ? rsGetCategories() : RS_DEFAULT_CATEGORIES;
  const cat = (cats && cats[catKey]) ? cats[catKey] : { label: "Skincare", color: "#F4DCC4", accent: "#C97B4A", shape: "jar" };
  const bg = cat.color || "#F4DCC4", accent = cat.accent || "#C97B4A";
  const id = "g" + (catKey || "gen") + (seed || 1);
  const hueShift = (((seed || 1) * 17) % 46) - 23;
  const shapeKey = (cat.shape && ["dropper","pump","jar","tube","spray"].includes(cat.shape)) ? cat.shape : "jar";
  const shapes = {
    dropper: `
      <ellipse cx="110" cy="205" rx="70" ry="14" fill="${accent}" opacity="0.12"/>
      <rect x="72" y="90" width="76" height="105" rx="14" fill="url(#${id})" stroke="${accent}" stroke-width="2"/>
      <rect x="86" y="55" width="48" height="42" rx="8" fill="${accent}" opacity="0.85"/>
      <rect x="96" y="30" width="28" height="30" rx="6" fill="${accent}"/>
      <line x1="110" y1="60" x2="110" y2="150" stroke="${accent}" stroke-width="3" opacity="0.5"/>
      <circle cx="110" cy="150" r="4" fill="${accent}"/>
      <path d="M85 120 Q110 108 135 120" stroke="#fff" stroke-width="2" opacity="0.5" fill="none"/>`,
    pump: `
      <ellipse cx="110" cy="205" rx="66" ry="14" fill="${accent}" opacity="0.12"/>
      <rect x="76" y="80" width="68" height="115" rx="20" fill="url(#${id})" stroke="${accent}" stroke-width="2"/>
      <rect x="92" y="55" width="36" height="30" rx="6" fill="${accent}"/>
      <rect x="100" y="35" width="20" height="24" rx="4" fill="${accent}" opacity="0.85"/>
      <path d="M120 40 L146 28" stroke="${accent}" stroke-width="6" stroke-linecap="round"/>
      <path d="M88 115 Q110 100 132 115" stroke="#fff" stroke-width="2" opacity="0.5" fill="none"/>`,
    jar: `
      <ellipse cx="110" cy="200" rx="72" ry="13" fill="${accent}" opacity="0.12"/>
      <rect x="66" y="95" width="88" height="90" rx="18" fill="url(#${id})" stroke="${accent}" stroke-width="2"/>
      <rect x="62" y="78" width="96" height="26" rx="10" fill="${accent}"/>
      <rect x="62" y="78" width="96" height="8" rx="4" fill="#fff" opacity="0.25"/>
      <ellipse cx="110" cy="130" rx="34" ry="10" fill="#fff" opacity="0.35"/>`,
    tube: `
      <ellipse cx="110" cy="205" rx="46" ry="10" fill="${accent}" opacity="0.12"/>
      <path d="M84 70 L136 70 L146 190 Q146 200 132 200 L88 200 Q74 200 74 190 Z" fill="url(#${id})" stroke="${accent}" stroke-width="2"/>
      <rect x="80" y="52" width="60" height="22" rx="6" fill="${accent}"/>
      <rect x="90" y="35" width="40" height="20" rx="10" fill="${accent}" opacity="0.85"/>
      <path d="M88 110 Q110 98 132 110" stroke="#fff" stroke-width="2" opacity="0.5" fill="none"/>`,
    spray: `
      <ellipse cx="110" cy="205" rx="60" ry="13" fill="${accent}" opacity="0.12"/>
      <rect x="80" y="90" width="60" height="108" rx="14" fill="url(#${id})" stroke="${accent}" stroke-width="2"/>
      <rect x="90" y="60" width="40" height="34" rx="8" fill="${accent}"/>
      <rect x="122" y="45" width="30" height="16" rx="6" fill="${accent}" opacity="0.85" transform="rotate(-18 122 45)"/>
      <path d="M150 38 L160 30 M154 44 L166 40 M150 50 L162 52" stroke="${accent}" stroke-width="2" opacity="0.6" stroke-linecap="round"/>
      <path d="M92 130 Q110 118 128 130" stroke="#fff" stroke-width="2" opacity="0.5" fill="none"/>`
  };
  return `
  <svg viewBox="0 0 220 230" xmlns="http://www.w3.org/2000/svg" style="filter:hue-rotate(${hueShift}deg) saturate(1.05);">
    <defs>
      <linearGradient id="${id}" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0" stop-color="#ffffff"/>
        <stop offset="1" stop-color="${bg}"/>
      </linearGradient>
    </defs>
    <circle cx="110" cy="115" r="100" fill="${bg}" opacity="0.35"/>
    ${shapes[shapeKey] || shapes.jar}
    <path d="M172 48 q7 4 3 11 q-4 6 -11 2 q4 8 -4 12 q-8 3 -10 -6" fill="none" stroke="${accent}" stroke-width="2" opacity="0.55" stroke-linecap="round"/>
  </svg>`;
}

/* ===========================================================
   DYNAMIC STORAGE & MYSQL DB SYNC (Categories & Products)
   =========================================================== */

function rsGetCategories() {
  const saved = localStorage.getItem("rs_categories");
  if (saved) {
    try {
      const parsed = JSON.parse(saved);
      if (parsed && typeof parsed === "object") {
        return parsed;
      }
    } catch(e){}
  }
  return RS_DEFAULT_CATEGORIES;
}

function rsSaveCategories(cats) {
  localStorage.setItem("rs_categories", JSON.stringify(cats));
}

function rsAddCategory(key, data) {
  const cats = { ...rsGetCategories() };
  cats[key] = {
    label: data.label || key,
    color: data.color || "#F4DCC4",
    accent: data.accent || "#C97B4A",
    shape: data.shape || "jar"
  };
  rsSaveCategories(cats);

  fetch("http://localhost:8080/api/categories", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ key: key, label: data.label || key, color: data.color || "#F4DCC4", accent: data.accent || "#C97B4A", shape: data.shape || "jar" })
  }).catch(function(err){ console.warn("Backend API offline", err); });

  return cats;
}

function rsDeleteCategory(key) {
  const cats = { ...rsGetCategories() };
  delete cats[key];
  rsSaveCategories(cats);

  fetch("http://localhost:8080/api/categories?key=" + encodeURIComponent(key), {
    method: "DELETE"
  }).catch(function(err){ console.warn("Backend API offline", err); });

  return cats;
}

function rsGetProducts() {
  const saved = localStorage.getItem("rs_products");
  if (saved) {
    try {
      const parsed = JSON.parse(saved);
      if (Array.isArray(parsed)) {
        return parsed;
      }
    } catch(e){}
  }
  return RS_DEFAULT_PRODUCTS;
}

function rsSaveProducts(prods) {
  localStorage.setItem("rs_products", JSON.stringify(prods));
}

function rsAddProduct(product) {
  const list = [...rsGetProducts()];
  if (!product.id) {
    product.id = "P" + String(Date.now()).slice(-5);
  }
  product.price = Number(product.price) || 0;
  product.oldPrice = product.oldPrice ? Number(product.oldPrice) : null;
  product.discount = product.discount ? Number(product.discount) : 0;
  product.rating = product.rating ? Number(product.rating) : 4.5;
  product.reviews = product.reviews ? Number(product.reviews) : 0;
  product.svgSeed = product.svgSeed || (Math.floor(Math.random() * 60) + 1);

  const cats = rsGetCategories();
  if (cats[product.category]) {
    product.categoryLabel = cats[product.category].label;
  } else {
    product.categoryLabel = product.category || "Skincare";
  }

  list.unshift(product);
  rsSaveProducts(list);

  fetch("http://localhost:8080/api/products", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      id: product.id,
      name: product.name,
      brand: product.brand,
      category: product.category,
      price: product.price,
      oldPrice: product.oldPrice || 0,
      discount: product.discount || 0,
      image: product.image || "",
      desc: product.desc || ""
    })
  }).then(async function(res) {
    if (res.ok) {
      const data = await res.json().catch(()=>({}));
      if (data && data.id) {
        product.id = String(data.id);
        rsSaveProducts(list);
      }
    }
  }).catch(function(err){ console.warn("Backend API offline", err); });

  return product;
}

function rsUpdateProduct(updatedProduct) {
  const list = [...rsGetProducts()];
  const idx = list.findIndex(p => String(p.id) === String(updatedProduct.id));
  if (idx !== -1) {
    updatedProduct.price = Number(updatedProduct.price) || 0;
    updatedProduct.oldPrice = updatedProduct.oldPrice ? Number(updatedProduct.oldPrice) : null;
    updatedProduct.discount = updatedProduct.discount ? Number(updatedProduct.discount) : 0;
    
    const cats = rsGetCategories();
    if (cats[updatedProduct.category]) {
      updatedProduct.categoryLabel = cats[updatedProduct.category].label;
    } else {
      updatedProduct.categoryLabel = updatedProduct.category || "Skincare";
    }

    list[idx] = { ...list[idx], ...updatedProduct };
    rsSaveProducts(list);

    fetch("http://localhost:8080/api/products", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        id: updatedProduct.id,
        name: updatedProduct.name,
        brand: updatedProduct.brand,
        category: updatedProduct.category,
        price: updatedProduct.price,
        oldPrice: updatedProduct.oldPrice || 0,
        discount: updatedProduct.discount || 0,
        image: updatedProduct.image || "",
        desc: updatedProduct.desc || ""
      })
    }).catch(function(err){ console.warn("Backend API offline", err); });

    return true;
  }
  return false;
}

function rsDeleteProduct(id) {
  const list = rsGetProducts();
  const filtered = list.filter(p => String(p.id) !== String(id));
  rsSaveProducts(filtered);

  fetch("http://localhost:8080/api/products?id=" + encodeURIComponent(id), {
    method: "DELETE"
  }).catch(function(err){ console.warn("Backend API offline", err); });

  return filtered;
}

function rsResetToDefaultProducts() {
  localStorage.removeItem("rs_products");
  localStorage.removeItem("rs_categories");
}

function rsFindProduct(id) {
  return rsGetProducts().find(p => String(p.id) === String(id));
}

function rsProductsByCategory(catKey) {
  const list = rsGetProducts();
  return catKey === "all" ? list : list.filter(p => p.category === catKey);
}

// ---------- BACKEND SYNC FUNCTION ----------
async function rsSyncFromBackend() {
  let updated = false;
  try {
    const cRes = await fetch("http://localhost:8080/api/categories");
    if (cRes.ok) {
      const cats = await cRes.json();
      if (cats && typeof cats === "object") {
        localStorage.setItem("rs_categories", JSON.stringify(cats));
        updated = true;
      }
    }
  } catch(e){}

  try {
    const pRes = await fetch("http://localhost:8080/api/products");
    if (pRes.ok) {
      const prods = await pRes.json();
      if (Array.isArray(prods)) {
        const cats = rsGetCategories();
        prods.forEach(p => {
          if (cats[p.category]) p.categoryLabel = cats[p.category].label;
        });
        localStorage.setItem("rs_products", JSON.stringify(prods));
        updated = true;
      }
    }
  } catch(e){}

  if (updated) {
    if (typeof window.renderHome === "function") window.renderHome();
    if (typeof window.renderCategoryFilters === "function") window.renderCategoryFilters();
    if (typeof window.renderGrid === "function") window.renderGrid();
    if (typeof window.render === "function") window.render();
    if (typeof window.populateCategoryDropdowns === "function") window.populateCategoryDropdowns();
  }
}

// Run backend sync immediately on script load
rsSyncFromBackend();

// Global live Proxies for backward compatibility
var RS_PRODUCTS = new Proxy([], {
  get(target, prop) {
    const list = rsGetProducts();
    if (prop === 'length') return list.length;
    if (typeof list[prop] === 'function') {
      return list[prop].bind(list);
    }
    return list[prop];
  },
  set(target, prop, value) {
    const list = rsGetProducts();
    list[prop] = value;
    rsSaveProducts(list);
    return true;
  }
});

var RS_CATEGORIES = new Proxy({}, {
  get(target, prop) {
    const cats = rsGetCategories();
    return cats[prop];
  },
  set(target, prop, value) {
    const cats = rsGetCategories();
    cats[prop] = value;
    rsSaveCategories(cats);
    return true;
  },
  ownKeys() {
    return Object.keys(rsGetCategories());
  },
  getOwnPropertyDescriptor(target, prop) {
    return Object.getOwnPropertyDescriptor(rsGetCategories(), prop);
  }
});

// ---------- Real product photo (falls back to SVG illustration if photo is missing) ----------
function rsProductImage(product){
  if(!product) return '';
  const seed = product.svgSeed || 1;
  const imgSrc = product.image ? product.image.trim() : '';
  if(!imgSrc) {
    return rsProductSVG(product.category, seed);
  }
  return `<img src="${imgSrc}" alt="${product.name || 'Product'}" loading="lazy" style="width:100%;height:100%;object-fit:contain;" onerror="this.onerror=null; this.outerHTML=rsProductSVG('${product.category}', ${seed});">`;
}