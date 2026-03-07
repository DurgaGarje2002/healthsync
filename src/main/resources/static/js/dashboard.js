// ================= INITIALIZATION =================

document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    const userName = localStorage.getItem('userName') || sessionStorage.getItem('userName');
    const userEmail = localStorage.getItem('email') || sessionStorage.getItem('email');

    if (!token || !userId) {
        window.location.href = '/login';
        return;
    }

    if (userName) {
        const initials = userName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
        document.getElementById('userAvatarSidebar').textContent = initials;
        document.getElementById('topbarAvatar').textContent = initials;
        document.getElementById('userNameSidebar').textContent = userName;
        document.getElementById('welcomeName').textContent = `Welcome back, ${userName.split(' ')[0]}!`;
    }
    if (userEmail) {
        document.getElementById('userEmailSidebar').textContent = userEmail;
    }

    const today = new Date().toISOString().split('T')[0];
    const dateEl = document.getElementById('date');
    if (dateEl) dateEl.value = today;
    const docDateEl = document.getElementById('docDate');
    if (docDateEl) docDateEl.value = today;

    const pageDate = document.getElementById('pageDate');
    if (pageDate) {
        pageDate.textContent = new Date().toLocaleDateString('en-US', {
            weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
        });
    }

    loadHealthRecords();
    loadDocuments();
    updateStats();
    loadNotifications();
    setupAvatarDropdown();

    document.addEventListener('sectionChanged', function(e) {
        if (e.detail === 'appointments') loadAppointments();
    });

    setupNavigation();
    setupMobileSidebar();
    setupDragDrop();

    const hash = window.location.hash.replace('#', '');
    if (hash && ['overview','records','documents','add-record'].includes(hash)) {
        switchSection(hash);
    }

    const role = localStorage.getItem('role') || sessionStorage.getItem('role');
    if (role === 'ROLE_ADMIN') {
        const adminLink = document.createElement('a');
        adminLink.href = '/admin-dashboard';
        adminLink.className = 'nav-item';
        adminLink.innerHTML = '<i class="fas fa-shield-alt"></i><span>Admin Panel</span>';
        adminLink.style.color = '#FCD34D';
        const nav = document.querySelector('.sidebar-nav');
        if (nav) nav.insertBefore(adminLink, nav.firstChild);
    }
});

// ================= NAVIGATION =================

function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item[data-section]');
    navItems.forEach(item => {
        item.addEventListener('click', function (e) {
            e.preventDefault();
            const section = this.getAttribute('data-section');
            switchSection(section);
        });
    });
}

function switchSection(sectionId) {
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
    const activeNav = document.querySelector(`.nav-item[data-section="${sectionId}"]`);
    if (activeNav) activeNav.classList.add('active');

    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    const activeSection = document.getElementById(`section-${sectionId}`);
    if (activeSection) activeSection.classList.add('active');

    const titles = {
        'overview':     'Overview',
        'records':      'Health Records',
        'documents':    'Documents',
        'add-record':   'Add Record',
        'appointments': 'Appointments'
    };
    const pageTitle = document.getElementById('pageTitle');
    if (pageTitle && titles[sectionId]) pageTitle.textContent = titles[sectionId];

    if (sectionId === 'appointments') loadAppointments();

    document.getElementById('sidebar').classList.remove('open');
}

function setupMobileSidebar() {
    const mobileBtn = document.getElementById('mobileMenuBtn');
    const sidebar = document.getElementById('sidebar');
    if (mobileBtn && sidebar) {
        mobileBtn.addEventListener('click', () => sidebar.classList.toggle('open'));
        document.addEventListener('click', (e) => {
            if (!sidebar.contains(e.target) && !mobileBtn.contains(e.target)) {
                sidebar.classList.remove('open');
            }
        });
    }
}

// ================= LOGOUT =================

function logout() {
    localStorage.clear();
    sessionStorage.clear();
    window.location.href = '/login';
}

// ================= EMERGENCY CARD QR POPUP =================

function openEmergencyCard() {
    const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    const token  = localStorage.getItem('token')  || sessionStorage.getItem('token');
    if (!userId) { showToast('User not found. Please login again.', 'error'); return; }
    if (!token)  { showToast('Session expired. Please login again.', 'error'); setTimeout(() => logout(), 2000); return; }
    fetchAndDisplayQRCard(userId, token);
}

async function fetchAndDisplayQRCard(userId, token) {
    showLoading();
    try {
        const qrResponse = await fetch('/api/patient-card/user/' + userId + '/qr', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (qrResponse.status === 401 || qrResponse.status === 403) throw new Error('Session expired. Please login again.');
        if (qrResponse.status === 404) throw new Error('Emergency card not found. Please create your emergency card first.');
        if (!qrResponse.ok) throw new Error('Failed to generate QR code. Please try again.');
        // Convert QR to base64 so it works in popup windows (blob URLs are origin-locked)
        const blob     = await qrResponse.blob();
        const imageUrl = await new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload  = () => resolve(reader.result);
            reader.onerror = () => reject(new Error('Failed to read QR image'));
            reader.readAsDataURL(blob);
        });

        const cardResponse = await fetch('/api/patient-card/user/' + userId, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const card = cardResponse.ok ? await cardResponse.json() : {};

        const patientName  = localStorage.getItem('userName') || sessionStorage.getItem('userName') || card.patientName || 'Patient';
        const risk         = card.riskLevel || 'UNKNOWN';
        const contact      = card.emergencyContact || '';
        const contactName  = card.contactName || '';
        const bloodType    = card.bloodType || '';
        const disease      = card.currentDisease || '';
        const allergy      = card.allergies || '';
        const meds         = card.medications || '';
        const hospital     = card.hospitalName || '';
        const hospitalAddr = card.hospitalAddress || '';
        const mapLink      = card.hospitalMapLink || '';
        const initials     = patientName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0,2) || 'P';
        const scanUrl      = window.location.origin + '/emergency/' + (card.publicId || '');

        const riskPalette = {
            HIGH:    { bg: 'linear-gradient(135deg,#DC2626,#B91C1C)', icon: '&#128680;', label: 'HIGH RISK'   },
            MEDIUM:  { bg: 'linear-gradient(135deg,#D97706,#B45309)', icon: '&#9888;&#65039;',  label: 'MEDIUM RISK' },
            LOW:     { bg: 'linear-gradient(135deg,#16A34A,#15803D)', icon: '&#9989;',  label: 'LOW RISK'    },
            UNKNOWN: { bg: 'linear-gradient(135deg,#3B82F6,#2563EB)', icon: '&#127973;', label: 'MEDICAL CARD'}
        };
        const rp = riskPalette[risk] || riskPalette.UNKNOWN;

        const qrWindow = window.open('', '_blank', 'width=700,height=900,scrollbars=yes,resizable=yes');
        if (!qrWindow) throw new Error('Popup blocked. Please allow popups for this site.');

        // ── Build HTML using safe string concatenation (no nested backticks) ──
        var allergyHtml = allergy
            ? '<div class="allergy-box"><div class="allergy-label">&#9888; ALLERGY ALERT</div><div class="allergy-val">' + allergy + '</div></div>'
            : '';
        var bloodHtml = bloodType
            ? '&#129978; Blood: <strong>' + bloodType + '</strong> &nbsp;&middot;&nbsp;'
            : '';
        var medsHtml = meds
            ? '<div class="section"><div class="section-tag"><i class="fas fa-pills"></i> Medications</div><div class="section-val">' + meds + '</div></div>'
            : '';
        var hospHtml = '';
        if (hospital) {
            hospHtml = '<div class="section"><div class="section-tag"><i class="fas fa-hospital"></i> Preferred Hospital</div><div class="section-val">' + hospital;
            if (hospitalAddr) hospHtml += '<div style="font-size:.78rem;color:#64748B;margin-top:2px">' + hospitalAddr + '</div>';
            hospHtml += '</div></div>';
        }
        var callHtml = contact
            ? '<button class="btn-call" onclick=\'window.location="tel:' + contact + '"\'><i class="fas fa-phone-alt"></i> ' + (contactName ? contactName + ' \u2014 ' : '') + contact + '</button>'
            : '<button class="btn-call" style="opacity:.5;cursor:default"><i class="fas fa-phone-alt"></i> No Emergency Contact on File</button>';
        var mapHtml  = mapLink
            ? '<a class="btn-map" href="' + mapLink + '" target="_blank" rel="noopener"><i class="fas fa-map-marker-alt"></i> Open Hospital on Google Maps</a>'
            : '';
        var dlName = 'healthsync-qr-' + patientName.replace(/\s+/g, '-') + '.png';

        // ── Build popup HTML: new card layout (risk header | QR+contact | allergy | rows) ──
        var H = '';
        H += '<!DOCTYPE html><html lang="en"><head>';
        H += '<meta charset="UTF-8">';
        H += '<meta name="viewport" content="width=device-width,initial-scale=1">';
        H += '<title>Emergency Card \u2014 ' + patientName + '</title>';
        H += '<link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Sora:wght@700;800;900&display=swap" rel="stylesheet">';
        H += '<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">';
        H += '<script src="https://cdn.jsdelivr.net/npm/html2canvas@1.4.1/dist/html2canvas.min.js"><\/script>';
        H += '<script src="https://cdn.jsdelivr.net/npm/jspdf@2.5.1/dist/jspdf.umd.min.js"><\/script>';
        H += '<style>';
        H += '*{margin:0;padding:0;box-sizing:border-box;}';
        H += 'body{font-family:"DM Sans",sans-serif;background:#1E293B;min-height:100vh;display:flex;flex-direction:column;align-items:center;padding:20px 12px 40px;}';

        // Action bar
        H += '.act{width:100%;max-width:520px;display:flex;gap:9px;margin-bottom:14px;}';
        H += '.ab{flex:1;padding:10px;border:none;border-radius:9px;font-size:.82rem;font-weight:700;font-family:"DM Sans",sans-serif;cursor:pointer;display:flex;align-items:center;justify-content:center;gap:7px;transition:all .2s;}';
        H += '.ab:hover{opacity:.9;transform:translateY(-1px);}';
        H += '.ab-png{background:linear-gradient(135deg,#0EA5E9,#0284C7);color:white;}';
        H += '.ab-pdf{background:linear-gradient(135deg,#EF4444,#DC2626);color:white;}';
        H += '.ab-close{background:rgba(255,255,255,.12);color:white;border:1px solid rgba(255,255,255,.2);}';

        // Card
        H += '.ec{background:white;border-radius:18px;overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,.4);width:100%;max-width:520px;}';

        // ① Risk header
        H += '.ec-hdr{padding:18px 22px;display:flex;align-items:center;justify-content:space-between;color:white;background:' + rp.bg + '}';
        H += '.ec-hl{display:flex;align-items:center;gap:12px}';
        H += '.ec-icon{font-size:1.8rem;}';
        H += '.ec-rlabel{font-size:.72rem;font-weight:700;letter-spacing:.6px;text-transform:uppercase;opacity:.85;}';
        H += '.ec-rval{font-family:"Sora",sans-serif;font-size:1.2rem;font-weight:900;margin-top:1px;}';
        H += '.ec-verified{background:rgba(255,255,255,.2);border:1px solid rgba(255,255,255,.35);border-radius:20px;padding:5px 13px;font-size:.68rem;font-weight:700;letter-spacing:.4px;}';

        // ② Middle grid
        H += '.ec-mid{display:grid;grid-template-columns:140px 1fr;border-bottom:1px solid #F1F5F9;}';
        H += '.ec-qc{padding:18px 14px;display:flex;flex-direction:column;align-items:center;justify-content:center;border-right:1px solid #F1F5F9;background:#FAFBFF;}';
        H += '.ec-qi{width:108px;height:108px;border-radius:10px;border:2px solid #E2E8F0;object-fit:contain;background:#F8FAFC;}';
        H += '.ec-qh{font-size:.6rem;color:#94A3B8;text-align:center;margin-top:7px;line-height:1.4;}';
        H += '.ec-ic{padding:16px;display:flex;flex-direction:column;justify-content:center;}';
        H += '.ec-pn{font-family:"Sora",sans-serif;font-size:1.05rem;font-weight:900;color:#0F172A;margin-bottom:4px;}';
        H += '.ec-bb{display:none;align-items:center;gap:5px;background:#FEE2E2;color:#B91C1C;border-radius:20px;padding:3px 10px;font-size:.7rem;font-weight:700;margin-bottom:10px;width:fit-content;}';
        H += '.ec-cb{background:#F8FAFC;border:1.5px solid #E2E8F0;border-radius:10px;padding:10px 13px;}';
        H += '.ec-cl{font-size:.63rem;font-weight:700;color:#94A3B8;letter-spacing:.6px;text-transform:uppercase;margin-bottom:3px;}';
        H += '.ec-cn{font-size:.92rem;font-weight:700;color:#0F172A;}';
        H += '.ec-cp{font-size:.85rem;color:#2563EB;font-weight:600;margin-top:2px;}';
        H += '.ec-cr{font-size:.7rem;color:#64748B;margin-top:2px;}';

        // ③ Allergy
        H += '.ec-al{margin:12px 16px 0;padding:10px 14px;background:#FEF2F2;border:1.5px solid #FCA5A5;border-radius:9px;}';
        H += '.ec-all{font-size:.64rem;font-weight:800;color:#DC2626;letter-spacing:.5px;text-transform:uppercase;margin-bottom:3px;}';
        H += '.ec-alv{font-size:.85rem;color:#7F1D1D;font-weight:600;}';

        // ④ Rows
        H += '.ec-rows{padding:8px 0 4px;}';
        H += '.ec-row{display:flex;align-items:flex-start;justify-content:space-between;padding:7px 18px;border-bottom:1px solid #F8FAFC;}';
        H += '.ec-row:last-child{border-bottom:none;}';
        H += '.ec-rl{font-size:.67rem;font-weight:700;color:#94A3B8;letter-spacing:.4px;text-transform:uppercase;flex-shrink:0;padding-top:1px;display:flex;align-items:center;gap:5px;}';
        H += '.ec-rv{font-size:.84rem;font-weight:600;color:#0F172A;text-align:right;max-width:62%;line-height:1.4;}';

        // Call/map
        H += '.ec-act{padding:12px 16px 8px;display:flex;flex-direction:column;gap:8px;}';
        H += '.btn-call{display:flex;align-items:center;justify-content:center;gap:8px;width:100%;padding:13px;border-radius:11px;background:#EF4444;color:white;border:none;font-size:.9rem;font-weight:700;font-family:"DM Sans",sans-serif;cursor:pointer;text-decoration:none;transition:all .2s;}';
        H += '.btn-map{display:flex;align-items:center;justify-content:center;gap:8px;width:100%;padding:11px;border-radius:11px;background:#1E40AF;color:white;border:none;font-size:.85rem;font-weight:700;font-family:"DM Sans",sans-serif;cursor:pointer;text-decoration:none;transition:all .2s;}';

        // Footer
        H += '.ec-ft{padding:12px 18px 16px;display:flex;align-items:center;justify-content:space-between;border-top:1px solid #F1F5F9;margin-top:4px;}';
        H += '.ec-fb{font-family:"Sora",sans-serif;font-size:.78rem;font-weight:800;color:#0F172A;}';
        H += '.ec-fb span{color:#2563EB}.ec-ftm{font-size:.65rem;color:#94A3B8;}';

        H += '@media print{body{background:white;padding:0}.act{display:none}.ec-act{display:none}.ec{box-shadow:none;border-radius:0;}}';
        H += '</style></head><body>';

        // ── Action bar ──
        H += '<div class="act">';
        H +=   '<button class="ab ab-png" onclick="dlPNG()"><i class="fas fa-image"></i> Download PNG</button>';
        H +=   '<button class="ab ab-pdf" onclick="dlPDF()"><i class="fas fa-file-pdf"></i> Download PDF</button>';
        H +=   '<button class="ab ab-close" onclick="window.print()"><i class="fas fa-print"></i> Print</button>';
        H += '</div>';

        // ── Card ──
        H += '<div class="ec" id="ec">';

        // ① Header
        H +=   '<div class="ec-hdr">';
        H +=     '<div class="ec-hl"><span class="ec-icon">' + rp.icon + '</span>';
        H +=     '<div><div class="ec-rlabel">RISK LEVEL</div><div class="ec-rval">' + rp.label + '</div></div></div>';
        H +=     '<div class="ec-verified"><i class="fas fa-shield-alt"></i> VERIFIED</div>';
        H +=   '</div>';

        // ② Middle
        H +=   '<div class="ec-mid">';
        H +=     '<div class="ec-qc">';
        H +=       '<img class="ec-qi" src="' + imageUrl + '" alt="QR Code">';
        H +=       '<div class="ec-qh">Scan to view<br>full card</div>';
        H +=     '</div>';
        H +=     '<div class="ec-ic">';
        H +=       '<div class="ec-pn">' + patientName + '</div>';
        if (bloodType) {
        H +=       '<div class="ec-bb" style="display:inline-flex">\uD83E\uDE78 ' + bloodType + '</div>';
        }
        H +=       '<div class="ec-cb">';
        H +=         '<div class="ec-cl">Emergency Contact</div>';
        H +=         '<div class="ec-cn">' + (contactName || '\u2014') + '</div>';
        H +=         '<div class="ec-cp">' + (contact || '\u2014') + '</div>';
        if (card.relationship) {
        H +=         '<div class="ec-cr">' + card.relationship + '</div>';
        }
        H +=       '</div>';
        H +=     '</div>';
        H +=   '</div>';

        // ③ Allergy
        if (allergy) {
        H +=   '<div class="ec-al"><div class="ec-all">\u26A0 Allergy Alert</div><div class="ec-alv">' + allergy + '</div></div>';
        }

        // ④ Medical rows
        H +=   '<div class="ec-rows">';
        H +=     '<div class="ec-row"><span class="ec-rl"><i class="fas fa-stethoscope"></i> Condition</span><span class="ec-rv">' + (disease || '\u2014') + '</span></div>';
        if (meds) {
        H +=     '<div class="ec-row"><span class="ec-rl"><i class="fas fa-pills"></i> Medications</span><span class="ec-rv">' + meds + '</span></div>';
        }
        if (hospital) {
            var hospStr = hospital + (hospitalAddr ? '<br><span style="font-weight:400;font-size:.76rem;color:#64748B">' + hospitalAddr + '</span>' : '');
        H +=     '<div class="ec-row"><span class="ec-rl"><i class="fas fa-hospital"></i> Hospital</span><span class="ec-rv">' + hospStr + '</span></div>';
        }
        H +=   '</div>';

        // Call + Map
        H +=   '<div class="ec-act" id="ecAct">';
        if (contact) {
        H +=     '<a class="btn-call" href="tel:' + contact + '"><i class="fas fa-phone-alt"></i> ' + (contactName ? contactName + ' \u2014 ' : '') + contact + '</a>';
        }
        if (mapLink) {
        H +=     '<a class="btn-map" href="' + mapLink + '" target="_blank" rel="noopener"><i class="fas fa-map-marker-alt"></i> Open Hospital on Google Maps</a>';
        }
        H +=   '</div>';

        // Footer
        H +=   '<div class="ec-ft">';
        H +=     '<div class="ec-fb">Health<span>Sync</span></div>';
        H +=     '<div class="ec-ftm" id="eft"></div>';
        H +=   '</div>';
        H += '</div>'; // .ec

        // Inline script — download functions
        H += '<script>';
        H += 'document.getElementById("eft").textContent="Updated "+new Date().toLocaleDateString("en-IN",{day:"2-digit",month:"short",year:"numeric"});';
        H += 'async function dlPNG(){';
        H +=   'document.getElementById("ecAct").style.display="none";';
        H +=   'var c=await html2canvas(document.getElementById("ec"),{scale:3,useCORS:true,allowTaint:true,backgroundColor:"#ffffff"});';
        H +=   'document.getElementById("ecAct").style.display="flex";';
        H +=   'var a=document.createElement("a");a.href=c.toDataURL("image/png");a.download="HealthSync-Emergency-Card.png";a.click();';
        H += '}';
        H += 'async function dlPDF(){';
        H +=   'document.getElementById("ecAct").style.display="none";';
        H +=   'var c=await html2canvas(document.getElementById("ec"),{scale:3,useCORS:true,allowTaint:true,backgroundColor:"#ffffff"});';
        H +=   'document.getElementById("ecAct").style.display="flex";';
        H +=   'var img=c.toDataURL("image/png");';
        H +=   'var {jsPDF}=window.jspdf;';
        H +=   'var p=new jsPDF({orientation:"portrait",unit:"mm",format:"a6"});';
        H +=   'var w=p.internal.pageSize.getWidth();';
        H +=   'p.addImage(img,"PNG",0,0,w,(c.height*w)/c.width);';
        H +=   'p.save("HealthSync-Emergency-Card.pdf");';
        H += '}';
        H += '<\/script>';
        H += '</body></html>';

        qrWindow.document.write(H);
        qrWindow.document.close();
        // Inject base64 image URL into popup window after it's written
        qrWindow._qrDataUrl  = imageUrl;
        qrWindow._qrFileName = dlName;

        hideLoading();
        showToast('Emergency card opened! 🆘', 'success');
    } catch (error) {
        hideLoading();
        showToast(error.message, 'error');
        if (error.message.includes('Session') || error.message.includes('login')) {
            setTimeout(() => logout(), 2000);
        }
    }
}

// ================= HEALTH RECORDS =================

let allRecords = [];

async function loadHealthRecords() {
    const token  = localStorage.getItem('token')  || sessionStorage.getItem('token');
    const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    if (!token || !userId) return;
    try {
        const response = await fetch('/api/health-records/user/' + userId, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!response.ok) { if (response.status === 401 || response.status === 403) logout(); return; }
        const records = await response.json();
        allRecords = records;
        displayHealthRecords(records);
        displayRecentActivity(records);
        updateRecordBadge(records.length);
    } catch (error) {
        console.error('Load records error:', error);
    }
}

function displayHealthRecords(records) {
    const tbody   = document.getElementById('recordsTableBody');
    const countEl = document.getElementById('recordCount');
    if (!tbody) return;

    _allRecordsForChart = records;
    initHealthChart(records);

    if (countEl) countEl.textContent = records.length + ' record' + (records.length !== 1 ? 's' : '');

    if (!records || records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-cell"><div class="empty-state-tbl"><i class="fas fa-clipboard-list"></i><p>No health records yet. Add your first record!</p></div></td></tr>';
        return;
    }

    const sorted = [...records].sort((a, b) => new Date(b.recordDate) - new Date(a.recordDate));
    tbody.innerHTML = sorted.map(r => `
        <tr>
            <td><strong>${formatDate(r.recordDate)}</strong></td>
            <td>${r.weight ? `<strong>${r.weight}</strong> kg` : '<span class="text-dim">—</span>'}</td>
            <td>${r.bloodPressure ? `<strong>${r.bloodPressure}</strong> mmHg` : '<span class="text-dim">—</span>'}</td>
            <td>${r.sugarLevel ? `<strong>${r.sugarLevel}</strong> mg/dL` : '<span class="text-dim">—</span>'}</td>
            <td><span class="type-badge ${(r.recordType || 'general').toLowerCase()}">${r.recordType || 'General'}</span></td>
            <td>${r.notes ? r.notes : '<span class="text-dim">No notes</span>'}</td>
            <td style="text-align:center;white-space:nowrap">
                <button class="tbl-action-btn edit-btn" title="Edit" onclick='editRecord(${JSON.stringify(r)})'>
                    <i class="fas fa-pen"></i>
                </button>
                <button class="tbl-action-btn del-btn" title="Delete" onclick="deleteRecord(${r.id}, this)">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>`).join('');
}

function displayRecentActivity(records) {
    const container = document.getElementById('recentActivity');
    if (!container) return;
    if (!records || records.length === 0) {
        container.innerHTML = '<div class="empty-mini"><i class="fas fa-heartbeat"></i><p>No records yet</p></div>';
        return;
    }
    const recent = [...records].sort((a, b) => new Date(b.recordDate) - new Date(a.recordDate)).slice(0, 5);
    container.innerHTML = recent.map(r => {
        const metric   = r.weight ? r.weight + ' kg' : r.bloodPressure ? r.bloodPressure : r.sugarLevel ? r.sugarLevel + ' mg/dL' : 'No metrics';
        const colorMap = { General: 'blue', Diabetes: 'purple', Heart: 'red', Fitness: 'green' };
        const color    = colorMap[r.recordType] || 'blue';
        return `<div class="recent-item">
            <div class="recent-dot ${color}"></div>
            <div class="recent-info"><div class="recent-title">${r.recordType || 'General'} Checkup</div>
            <div class="recent-date">${formatDate(r.recordDate)}</div></div>
            <div class="recent-metric">${metric}</div></div>`;
    }).join('');
}

function filterRecords() {
    const searchVal = (document.getElementById('searchRecords')?.value || '').toLowerCase();
    const typeVal   = document.getElementById('filterType')?.value || '';
    const filtered  = allRecords.filter(r => {
        const matchSearch = !searchVal ||
            (r.recordType || '').toLowerCase().includes(searchVal) ||
            (r.notes || '').toLowerCase().includes(searchVal) ||
            (r.recordDate || '').includes(searchVal);
        const matchType = !typeVal || (r.recordType || '') === typeVal;
        return matchSearch && matchType;
    });
    displayHealthRecords(filtered);
}

async function addHealthRecord(event) {
    event.preventDefault();
    const token  = localStorage.getItem('token')  || sessionStorage.getItem('token');
    const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    const weight = document.getElementById('weight').value;
    const bp     = document.getElementById('bp').value;
    const sugar  = document.getElementById('sugar').value;
    if (!weight && !bp && !sugar) { showToast('Please fill in at least one vital metric', 'error'); return; }
    const recordData = {
        userId,
        recordDate:    document.getElementById('date').value,
        weight:        weight ? parseFloat(weight) : null,
        bloodPressure: bp || null,
        sugarLevel:    sugar ? parseFloat(sugar) : null,
        recordType:    document.getElementById('type').value,
        notes:         document.getElementById('notes').value || null
    };
    showLoading();
    try {
        const response = await fetch('/api/health-records/user/' + userId, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' },
            body: JSON.stringify(recordData)
        });
        if (!response.ok) { if (response.status === 401 || response.status === 403) logout(); throw new Error('Failed'); }
        showToast('Health record saved successfully!', 'success');
        document.getElementById('recordForm').reset();
        document.getElementById('date').value = new Date().toISOString().split('T')[0];
        await loadHealthRecords();
        await updateStats();
        setTimeout(() => switchSection('records'), 800);
    } catch { showToast('Failed to save health record', 'error'); }
    finally { hideLoading(); }
}

// ================= STATISTICS =================

async function updateStats() {
    const token  = localStorage.getItem('token')  || sessionStorage.getItem('token');
    const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    if (!token || !userId) return;
    try {
        const response = await fetch('/api/health-records/user/' + userId, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!response.ok) return;
        const records = await response.json();
        document.getElementById('totalRecords').textContent = records.length;
        updateRecordBadge(records.length);
        if (records.length > 0) {
            const sorted = [...records].sort((a, b) => new Date(b.recordDate) - new Date(a.recordDate));
            const latest = sorted[0];
            setStatWithAnimation('latestWeight', latest.weight ? latest.weight + '' : '--');
            setStatWithAnimation('latestBP',     latest.bloodPressure || '--');
            setStatWithAnimation('latestSugar',  latest.sugarLevel ? latest.sugarLevel + '' : '--');
            let score = 60;
            if (latest.weight && latest.weight > 40 && latest.weight < 120) score += 10;
            if (latest.bloodPressure) {
                const parts = latest.bloodPressure.split('/');
                if (parts.length === 2) {
                    const sys = parseInt(parts[0]), dia = parseInt(parts[1]);
                    if (sys < 130 && dia < 85) score += 15;
                }
            }
            if (latest.sugarLevel && latest.sugarLevel > 70 && latest.sugarLevel < 140) score += 15;
            score = Math.min(score, 100);
            const scoreEl = document.getElementById('healthScore');
            if (scoreEl) scoreEl.textContent = score;
            const circle = document.getElementById('healthScoreCircle');
            if (circle) {
                const circumference = 2 * Math.PI * 45;
                circle.style.strokeDashoffset = circumference - (score / 100) * circumference;
            }
        }
    } catch (error) { console.error('Update stats error:', error); }
}

function setStatWithAnimation(elementId, value) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.style.opacity = '0'; el.style.transform = 'translateY(5px)';
    setTimeout(() => {
        el.textContent = value;
        el.style.transition = 'all 0.3s ease';
        el.style.opacity = '1'; el.style.transform = 'translateY(0)';
    }, 150);
}

function updateRecordBadge(count) {
    const badge = document.getElementById('recordBadge');
    if (badge) badge.textContent = count;
}

// ================= DOCUMENTS =================

function loadDocuments() {
    const documents = JSON.parse(localStorage.getItem('healthDocuments') || '[]');
    displayDocuments(documents);
}

function displayDocuments(documents) {
    const grid = document.getElementById('documentsGrid');
    if (!grid) return;
    if (!documents || documents.length === 0) {
        grid.innerHTML = `<div class="empty-docs-state">
            <div class="empty-icon-wrap"><i class="fas fa-file-medical"></i></div>
            <h4>No documents yet</h4>
            <p>Upload X-rays, prescriptions, lab results and more</p>
            <button class="btn-primary-sm" data-bs-toggle="modal" data-bs-target="#uploadModal">
                <i class="fas fa-plus me-1"></i>Upload First Document</button></div>`;
        return;
    }
    const sorted = [...documents].sort((a, b) => new Date(b.date) - new Date(a.date));
    const chipMap = { 'X-Ray':'chip-xray','Report':'chip-report','Prescription':'chip-prescription','Lab':'chip-lab','Other':'chip-other' };
    grid.innerHTML = sorted.map((doc, index) => {
        const chip  = chipMap[doc.type] || 'chip-other';
        const isPdf = doc.fileType === 'application/pdf';
        return `<div class="doc-card">
            <div class="doc-preview">${isPdf ? '<i class="fas fa-file-pdf doc-icon" style="color:#EF4444"></i>' : '<img src="' + doc.data + '" alt="' + doc.name + '" loading="lazy">'}</div>
            <div class="doc-info">
                <span class="doc-type-chip ${chip}">${doc.type}</span>
                <div class="doc-name">${doc.name}</div>
                <div class="doc-meta"><i class="fas fa-calendar me-1"></i>${formatDate(doc.date)} &nbsp;·&nbsp; <i class="fas fa-file me-1"></i>${formatFileSize(doc.fileSize)}</div>
            </div>
            <div class="doc-actions">
                <button class="doc-btn view" onclick="viewDocument(${index})"><i class="fas fa-eye me-1"></i>View</button>
                <button class="doc-btn delete" onclick="deleteDocument(${index})"><i class="fas fa-trash me-1"></i></button>
            </div></div>`;
    }).join('');
}

function handleFileSelect(event) {
    const file = event.target.files[0];
    if (!file) return;
    if (file.size > 5 * 1024 * 1024) { showToast('File size must be under 5MB', 'error'); event.target.value = ''; return; }
    const allowed = ['image/jpeg', 'image/png', 'image/jpg', 'application/pdf'];
    if (!allowed.includes(file.type)) { showToast('Only JPG, PNG, and PDF files are allowed', 'error'); event.target.value = ''; return; }
    const info = document.getElementById('selectedFileInfo');
    const nameEl = document.getElementById('selectedFileName');
    if (info && nameEl) { nameEl.textContent = file.name + ' (' + formatFileSize(file.size) + ')'; info.style.display = 'flex'; }
}

function clearFile() {
    document.getElementById('fileInput').value = '';
    const info = document.getElementById('selectedFileInfo');
    if (info) info.style.display = 'none';
}

async function uploadDocument(event) {
    event.preventDefault();
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];
    if (!file) { showToast('Please select a file', 'error'); return; }
    const docName = document.getElementById('docName').value.trim();
    if (!docName) { showToast('Please enter a document name', 'error'); return; }
    const progressWrap = document.getElementById('uploadProgress');
    const progressFill = document.getElementById('progressBar');
    if (progressWrap) progressWrap.style.display = 'block';
    let progress = 0;
    const interval = setInterval(() => {
        progress = Math.min(progress + 8, 90);
        if (progressFill) progressFill.style.width = progress + '%';
        if (progress >= 90) clearInterval(interval);
    }, 80);
    const reader = new FileReader();
    reader.onload = function(e) {
        clearInterval(interval);
        if (progressFill) progressFill.style.width = '100%';
        const doc = {
            name: docName, type: document.getElementById('docType').value,
            date: document.getElementById('docDate').value,
            notes: document.getElementById('docNotes').value,
            fileType: file.type, fileName: file.name, fileSize: file.size, data: e.target.result
        };
        const docs = JSON.parse(localStorage.getItem('healthDocuments') || '[]');
        docs.push(doc);
        localStorage.setItem('healthDocuments', JSON.stringify(docs));
        setTimeout(() => {
            bootstrap.Modal.getInstance(document.getElementById('uploadModal'))?.hide();
            loadDocuments();
            showToast('Document uploaded successfully!', 'success');
            document.getElementById('uploadForm').reset();
            if (progressWrap) progressWrap.style.display = 'none';
            if (progressFill) progressFill.style.width = '0%';
            clearFile();
        }, 400);
    };
    reader.onerror = () => { clearInterval(interval); showToast('Failed to read file. Try again.', 'error'); if (progressWrap) progressWrap.style.display = 'none'; };
    reader.readAsDataURL(file);
}

function viewDocument(index) {
    const documents = JSON.parse(localStorage.getItem('healthDocuments') || '[]');
    const doc = documents[index];
    if (!doc) return;
    document.getElementById('viewModalLabel').textContent = doc.name;
    const viewer = document.getElementById('documentViewer');
    viewer.innerHTML = doc.fileType === 'application/pdf'
        ? '<embed src="' + doc.data + '" type="application/pdf" width="100%" height="500px" style="border-radius:8px;">'
        : '<img src="' + doc.data + '" alt="' + doc.name + '" style="max-width:100%;border-radius:8px;box-shadow:0 4px 16px rgba(0,0,0,0.1)">';
    const dlLink = document.getElementById('downloadLink');
    if (dlLink) { dlLink.href = doc.data; dlLink.download = doc.fileName; }
    new bootstrap.Modal(document.getElementById('viewModal')).show();
}

function deleteDocument(index) {
    if (!confirm('Delete this document? This cannot be undone.')) return;
    const docs = JSON.parse(localStorage.getItem('healthDocuments') || '[]');
    const deleted = docs.splice(index, 1)[0];
    localStorage.setItem('healthDocuments', JSON.stringify(docs));
    loadDocuments();
    showToast('"' + deleted.name + '" deleted', 'success');
}

// ================= DRAG & DROP =================

function setupDragDrop() {
    const zone = document.getElementById('uploadArea');
    if (!zone) return;
    ['dragenter','dragover','dragleave','drop'].forEach(e => { zone.addEventListener(e, ev => { ev.preventDefault(); ev.stopPropagation(); }, false); });
    ['dragenter','dragover'].forEach(e => zone.addEventListener(e, () => zone.classList.add('dragover'), false));
    ['dragleave','drop'].forEach(e => zone.addEventListener(e, () => zone.classList.remove('dragover'), false));
    zone.addEventListener('drop', function(e) {
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            document.getElementById('fileInput').files = files;
            handleFileSelect({ target: document.getElementById('fileInput') });
        }
    });
}

// ================= UTILITIES =================

function showLoading() { const el = document.getElementById('loadingOverlay'); if (el) el.style.display = 'flex'; }
function hideLoading() { const el = document.getElementById('loadingOverlay'); if (el) el.style.display = 'none'; }

function showToast(message, type) {
    type = type || 'info';
    const container = document.getElementById('toastContainer');
    if (!container) return;
    const icons = { success: 'fa-check-circle', error: 'fa-exclamation-circle', info: 'fa-info-circle' };
    const toast = document.createElement('div');
    toast.className = 'toast-item ' + type;
    toast.innerHTML = '<i class="fas ' + (icons[type] || icons.info) + '"></i><span>' + message + '</span>';
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.transition = 'all 0.3s ease'; toast.style.opacity = '0'; toast.style.transform = 'translateX(20px)';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('en-US', { year:'numeric', month:'short', day:'numeric' });
}
function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024*1024) return (bytes/1024).toFixed(1) + ' KB';
    return (bytes/(1024*1024)).toFixed(1) + ' MB';
}

// ================= BELL NOTIFICATIONS =================

const DEFAULT_NOTIFICATIONS = [
    { icon:'fa-heartbeat', cls:'ni-blue',   text:'Welcome to HealthSync! Add your first health record.',           time:'Just now'  },
    { icon:'fa-id-card',   cls:'ni-green',  text:'Set up your Emergency Card so responders can help you faster.', time:'2 min ago' },
    { icon:'fa-shield-alt',cls:'ni-yellow', text:'Keep your profile updated for accurate health tracking.',        time:'1 hr ago'  },
];

function loadNotifications() {
    const saved = JSON.parse(localStorage.getItem('hs_notifications') || 'null');
    renderNotifications(saved || DEFAULT_NOTIFICATIONS);
}

function renderNotifications(list) {
    const el  = document.getElementById('notifList');
    const dot = document.getElementById('notifDot');
    if (!el) return;
    if (!list || list.length === 0) {
        el.innerHTML = '<div class="notif-empty"><i class="fas fa-bell-slash"></i>No notifications</div>';
        if (dot) dot.style.display = 'none'; return;
    }
    if (dot) dot.style.display = 'block';
    el.innerHTML = list.map(n =>
        '<div class="notif-item"><div class="notif-icon ' + n.cls + '"><i class="fas ' + n.icon + '"></i></div>' +
        '<div><div class="notif-text">' + n.text + '</div><div class="notif-time">' + n.time + '</div></div></div>'
    ).join('');
}

function clearAllNotifications() {
    localStorage.setItem('hs_notifications', '[]');
    renderNotifications([]);
}

function toggleNotifDropdown(e) {
    e.stopPropagation();
    const dd = document.getElementById('notifDropdown');
    const avatarDd = document.getElementById('avatarDropdown');
    if (avatarDd) avatarDd.classList.remove('open');
    dd.classList.toggle('open');
}

// ================= AVATAR DROPDOWN =================

function toggleAvatarDropdown(e) {
    e.stopPropagation();
    const dd = document.getElementById('avatarDropdown');
    const notifDd = document.getElementById('notifDropdown');
    if (notifDd) notifDd.classList.remove('open');
    dd.classList.toggle('open');
}

function setupAvatarDropdown() {
    const name     = localStorage.getItem('userName') || sessionStorage.getItem('userName') || 'User';
    const email    = localStorage.getItem('email')    || sessionStorage.getItem('email')    || '';
    const role     = localStorage.getItem('role')     || sessionStorage.getItem('role')     || '';
    const initials = name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) || 'U';
    const avName   = document.getElementById('avDropName');
    const avEmail  = document.getElementById('avDropEmail');
    const avAvatar = document.getElementById('avDropAvatar');
    if (avName)   avName.textContent   = name;
    if (avEmail)  avEmail.textContent  = email;
    if (avAvatar) avAvatar.textContent = initials;
    if (role === 'ROLE_ADMIN') {
        const adminLink = document.getElementById('avAdminLink');
        if (adminLink) adminLink.style.display = 'block';
    }
}

document.addEventListener('click', function(e) {
    const notifWrap  = document.getElementById('notifWrap');
    const avatarWrap = document.getElementById('avatarWrap');
    const notifDd    = document.getElementById('notifDropdown');
    const avatarDd   = document.getElementById('avatarDropdown');
    if (notifDd  && notifWrap  && !notifWrap.contains(e.target))  notifDd.classList.remove('open');
    if (avatarDd && avatarWrap && !avatarWrap.contains(e.target)) avatarDd.classList.remove('open');
});

// ================= DELETE RECORD =================

async function deleteRecord(recordId, btn) {
    if (!confirm('Delete this health record permanently? This cannot be undone.')) return;
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    const row   = btn ? btn.closest('tr') : null;
    if (row) row.style.opacity = '0.4';
    try {
        const res = await fetch('/api/health-records/' + recordId, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (res.ok) { showToast('Record deleted', 'success'); await loadHealthRecords(); }
        else { if (row) row.style.opacity = '1'; showToast('Failed to delete record', 'error'); }
    } catch(e) { if (row) row.style.opacity = '1'; showToast('Network error', 'error'); }
}

// ================= EDIT RECORD =================

function editRecord(record) {
    document.getElementById('editRecordId').value = record.id;
    document.getElementById('editDate').value     = record.recordDate     || '';
    document.getElementById('editWeight').value   = record.weight         || '';
    document.getElementById('editBP').value       = record.bloodPressure  || '';
    document.getElementById('editSugar').value    = record.sugarLevel     || '';
    document.getElementById('editType').value     = record.recordType     || 'General';
    document.getElementById('editNotes').value    = record.notes          || '';
    new bootstrap.Modal(document.getElementById('editRecordModal')).show();
}

async function saveEditRecord(event) {
    event.preventDefault();
    const token   = localStorage.getItem('token')  || sessionStorage.getItem('token');
    const userId  = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    const id      = document.getElementById('editRecordId').value;
    const payload = {
        userId,
        recordDate:    document.getElementById('editDate').value,
        weight:        document.getElementById('editWeight').value ? parseFloat(document.getElementById('editWeight').value) : null,
        bloodPressure: document.getElementById('editBP').value    || null,
        sugarLevel:    document.getElementById('editSugar').value ? parseFloat(document.getElementById('editSugar').value) : null,
        recordType:    document.getElementById('editType').value,
        notes:         document.getElementById('editNotes').value  || null,
    };
    try {
        const res = await fetch('/api/health-records/' + id, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            bootstrap.Modal.getInstance(document.getElementById('editRecordModal')).hide();
            showToast('Record updated successfully', 'success');
            await loadHealthRecords();
        } else { showToast('Failed to update record', 'error'); }
    } catch(e) { showToast('Network error', 'error'); }
}

// ================= HEALTH TREND CHART =================

let healthChartInstance = null;
let _allRecordsForChart = [];

function initHealthChart(records) {
    const canvas  = document.getElementById('healthChart');
    const emptyEl = document.getElementById('chartEmpty');
    if (!canvas) return;
    const metric   = document.getElementById('chartMetric')?.value || 'weight';
    const filtered = records
        .filter(r => metric === 'weight' ? r.weight : r.sugarLevel)
        .sort((a, b) => new Date(a.recordDate) - new Date(b.recordDate))
        .slice(-12);
    if (filtered.length === 0) {
        canvas.style.display = 'none';
        if (emptyEl) emptyEl.style.display = 'flex'; return;
    }
    canvas.style.display = 'block';
    if (emptyEl) emptyEl.style.display = 'none';
    const labels = filtered.map(r => new Date(r.recordDate).toLocaleDateString('en-IN', { day:'2-digit', month:'short' }));
    const data   = filtered.map(r => metric === 'weight' ? r.weight : r.sugarLevel);
    const label  = metric === 'weight' ? 'Weight (kg)' : 'Sugar Level (mg/dL)';
    const color  = metric === 'weight' ? '#3B82F6' : '#8B5CF6';
    if (healthChartInstance) healthChartInstance.destroy();
    healthChartInstance = new Chart(canvas, {
        type: 'line',
        data: { labels, datasets: [{ label, data, borderColor: color, backgroundColor: color + '18',
            borderWidth: 2.5, pointBackgroundColor: color, pointRadius: 4, pointHoverRadius: 6, fill: true, tension: 0.35 }] },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { display: false }, tooltip: { backgroundColor:'#1E293B', titleColor:'#94A3B8', bodyColor:'#F8FAFC', padding:10, cornerRadius:8 } },
            scales: {
                x: { grid: { color:'rgba(148,163,184,0.1)' }, ticks: { color:'#94A3B8', font:{ size:11 } } },
                y: { grid: { color:'rgba(148,163,184,0.1)' }, ticks: { color:'#94A3B8', font:{ size:11 } } }
            }
        }
    });
}

function updateChart() { initHealthChart(_allRecordsForChart); }

// ================= APPOINTMENTS =================

const APPT_STATUS_COLORS = {
    SCHEDULED: { bg:'#EFF6FF', color:'#1D4ED8' },
    COMPLETED:  { bg:'#F0FDF4', color:'#15803D' },
    CANCELLED:  { bg:'#FFF1F2', color:'#BE123C' },
};

async function loadAppointments() {
    const token  = localStorage.getItem('token')  || sessionStorage.getItem('token');
    const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    if (!token || !userId) return;
    const tbody = document.getElementById('appointmentsTableBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;padding:20px;color:var(--text-secondary)"><i class="fas fa-spinner fa-spin"></i> Loading...</td></tr>';
    try {
        const res = await fetch('/api/appointments/user/' + userId, { headers: { 'Authorization': 'Bearer ' + token } });
        if (!res.ok) { if (res.status === 401 || res.status === 403) logout(); return; }
        renderAppointments(await res.json());
    } catch(e) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-cell"><div class="empty-state-tbl"><i class="fas fa-exclamation-circle"></i><p>Failed to load appointments</p></div></td></tr>';
    }
}

function renderAppointments(appts) {
    const tbody = document.getElementById('appointmentsTableBody');
    if (!tbody) return;
    if (!appts || appts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-cell"><div class="empty-state-tbl"><i class="fas fa-calendar-times"></i><p>No appointments yet. Book your first one!</p></div></td></tr>';
        return;
    }
    const sorted = [...appts].sort((a, b) => new Date(b.appointmentDate) - new Date(a.appointmentDate));
    tbody.innerHTML = sorted.map(a => {
        const s       = APPT_STATUS_COLORS[a.status] || APPT_STATUS_COLORS.SCHEDULED;
        const dt      = new Date(a.appointmentDate);
        const dateStr = dt.toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' });
        const timeStr = dt.toLocaleTimeString('en-IN', { hour:'2-digit', minute:'2-digit', hour12:true });
        const isPast  = dt < new Date();
        return '<tr><td><div style="font-weight:600">' + dateStr + '</div><div style="font-size:0.8rem;color:var(--text-secondary)">' + timeStr + '</div></td>'
            + '<td><strong>' + (a.doctorName || a.clinicName || '—') + '</strong></td>'
            + '<td style="max-width:180px">' + (a.reason || '—') + '</td>'
            + '<td><span style="padding:4px 10px;border-radius:20px;font-size:0.78rem;font-weight:600;background:' + s.bg + ';color:' + s.color + '">' + (a.status || 'SCHEDULED') + '</span></td>'
            + '<td style="text-align:center;white-space:nowrap">'
            + (a.status === 'SCHEDULED'
                ? '<button class="tbl-action-btn edit-btn" title="Mark Completed" onclick="updateApptStatus(' + a.id + ',\'COMPLETED\')"><i class="fas fa-check"></i></button>'
                + '<button class="tbl-action-btn del-btn" title="Cancel" onclick="updateApptStatus(' + a.id + ',\'CANCELLED\')"><i class="fas fa-times"></i></button>'
                : '<span style="font-size:0.75rem;color:var(--text-secondary)">' + (isPast ? 'Past' : '—') + '</span>')
            + '</td></tr>';
    }).join('');
}

function openBookModal() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('apptDate').value   = today;
    document.getElementById('apptTime').value   = '10:00';
    document.getElementById('apptDoctor').value = '';
    document.getElementById('apptReason').value = '';
    new bootstrap.Modal(document.getElementById('bookApptModal')).show();
}

async function submitAppointment(event) {
    event.preventDefault();
    const token  = localStorage.getItem('token')  || sessionStorage.getItem('token');
    const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    const dateVal = document.getElementById('apptDate').value;
    const timeVal = document.getElementById('apptTime').value || '09:00';
    const payload = {
        userId:          parseInt(userId),
        appointmentDate: dateVal + 'T' + timeVal + ':00',
        doctorName:      document.getElementById('apptDoctor').value,
        reason:          document.getElementById('apptReason').value,
        status:          'SCHEDULED'
    };
    try {
        const res = await fetch('/api/appointments/user/' + userId, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            bootstrap.Modal.getInstance(document.getElementById('bookApptModal')).hide();
            showToast('Appointment booked successfully!', 'success');
            await loadAppointments();
        } else { showToast('Failed to book appointment', 'error'); }
    } catch(e) { showToast('Network error', 'error'); }
}

async function updateApptStatus(apptId, newStatus) {
    const label = newStatus === 'COMPLETED' ? 'mark as completed' : 'cancel this appointment';
    if (!confirm('Are you sure you want to ' + label + '?')) return;
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    try {
        const res = await fetch('/api/appointments/' + apptId + '/status', {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
            body: JSON.stringify({ status: newStatus })
        });
        if (res.ok) {
            showToast(newStatus === 'COMPLETED' ? 'Marked as completed' : 'Appointment cancelled', newStatus === 'COMPLETED' ? 'success' : 'info');
            await loadAppointments();
        } else { showToast('Failed to update status', 'error'); }
    } catch(e) { showToast('Network error', 'error'); }
}