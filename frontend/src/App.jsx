import { useMemo, useState, useEffect } from 'react'
import { BrowserRouter, Routes, Route, useNavigate, Navigate } from 'react-router-dom'
import './App.css'
import SignUpPage from './components/SignUpPage'
import MpinModal from './components/MpinModal'
import CustomerDashboard from './components/CustomerDashboard'
import ProtectedRoute from './components/ProtectedRoute'
import QaTriageDashboard from './components/QaTriageDashboard'

const Icon = ({ name, size = 22 }) => {
  const paths = {
    shield: <><path d="M12 3 4.5 6v5.5c0 4.3 3 7.7 7.5 9.5 4.5-1.8 7.5-5.2 7.5-9.5V6L12 3Z"/><path d="m9 12 2 2 4-4"/></>, wallet: <><path d="M4 7h14a2 2 0 0 1 2 2v9H4a2 2 0 0 1-2-2V6a3 3 0 0 1 3-3h12"/><path d="M16 13h.01"/></>, chart: <><path d="M4 19V5"/><path d="M4 19h16"/><path d="m7 15 4-4 3 2 5-6"/></>, card: <><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 10h18"/><path d="M7 15h3"/></>, scan: <><path d="M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3"/><path d="M8 10h8v5H8z"/></>, send: <><path d="m21 3-7.5 18-3.5-8-7-3.5L21 3Z"/><path d="m10 13 4-4"/></>, home: <><path d="m3 11 9-8 9 8"/><path d="M5 10v10h14V10"/></>, graduation: <><path d="m2 10 10-5 10 5-10 5L2 10Z"/><path d="M6 12v4c3 2 9 2 12 0v-4"/></>, car: <><path d="m5 16-1 3M19 16l1 3M3 13h18v5H3z"/><path d="m5 13 2-5h10l2 5"/><path d="M7 16h.01M17 16h.01"/></>, heart: <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8Z"/>, arrow: <><path d="M5 12h14"/><path d="m13 6 6 6-6 6"/></>, search: <><circle cx="11" cy="11" r="6"/><path d="m20 20-4.5-4.5"/></>, check: <path d="m5 12 4 4L19 6"/>,
  }
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name] || paths.chart}</svg>
}

const SectionIntro = ({ eyebrow, title, text, centered = true }) => <div className={`section-intro ${centered ? 'centered' : ''}`}><span className="eyebrow">{eyebrow}</span><h2>{title}</h2><p>{text}</p></div>
const ServiceCard = ({ icon, title, children, tint = '' }) => <article className={`service-card ${tint}`}><div className="icon-box"><Icon name={icon}/></div><h3>{title}</h3><p>{children}</p><a href="#support">Learn more <Icon name="arrow" size={16}/></a></article>

function EmiCalculator(){const[a,setA]=useState(750000),[r,setR]=useState(10.5),[y,setY]=useState(5);const x=useMemo(()=>{const m=Math.max(1,+y*12),q=+r/1200,e=q?(+a*q*Math.pow(1+q,m))/(Math.pow(1+q,m)-1):+a/m,t=e*m;return{e:Math.round(e),i:Math.round(t-a),t:Math.round(t)}},[a,r,y]);const f=n=>new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR',maximumFractionDigits:0}).format(n);return <div className="calculator"><div className="calc-heading"><span className="icon-box small"><Icon name="chart" size={18}/></span><div><h3>EMI Calculator</h3><p>Plan before you apply</p></div></div><div className="input-grid"><label>Loan Amount<input type="number" min="0" value={a} onChange={e=>setA(e.target.value)}/><span>₹</span></label><label>Interest Rate<input type="number" min="0" step="0.1" value={r} onChange={e=>setR(e.target.value)}/><span>% p.a.</span></label><label>Loan Tenure<input type="number" min="1" value={y} onChange={e=>setY(e.target.value)}/><span>Years</span></label></div><div className="emi-result"><div><small>Monthly EMI</small><strong>{f(x.e)}</strong></div><div><span>Total Interest</span><b>{f(x.i)}</b></div><div><span>Total Repayment</span><b>{f(x.t)}</b></div></div></div>}
function InsuranceEstimator(){const[a,setA]=useState(30),[m,setM]=useState(2),[c,setC]=useState(10);const e=Math.round((+c*750 + +a*28 + +m*480)/10)*10;return <div className="estimator"><h3>Health cover estimator</h3><p>Get a simple planning estimate in seconds.</p><div className="estimator-inputs"><label>Age<input type="number" value={a} min="18" max="100" onChange={z=>setA(z.target.value)}/></label><label>Family members<input type="number" value={m} min="1" max="10" onChange={z=>setM(z.target.value)}/></label><label>Desired coverage (₹ Lakh)<input type="number" value={c} min="1" onChange={z=>setC(z.target.value)}/></label></div><div className="estimate-result"><span>Estimated monthly premium</span><strong>₹{e.toLocaleString('en-IN')}<small> / month</small></strong></div><small className="disclaimer">Indicative planning estimate only. This is not an insurance quote.</small></div>}

// HomePage Component
function HomePage() {
  const navigate = useNavigate()
  const [menu, setMenu] = useState(false)
  const nav = ['Home', 'Banking', 'Loans', 'Insurance', 'Investments', 'Payments', 'Support']
  const payment = [['send', 'UPI Transfers'], ['scan', 'Scan & Pay'], ['send', 'Send Money'], ['wallet', 'Receive Money'], ['chart', 'Payment History'], ['card', 'Bill Payments']]

  return (
    <div className="app-shell">
      <header>
        <a className="logo" href="#home"><i>Y</i>YourBank</a>
        <button className="menu-button" aria-label="Toggle navigation" onClick={() => setMenu(!menu)}>☰</button>
        <nav className={menu ? 'open' : ''}>{nav.map(x => <a onClick={() => setMenu(false)} href={`#${x.toLowerCase()}`} key={x}>{x}</a>)}</nav>
        <div className="header-actions">
          <button className="text-button" onClick={() => navigate('/qa-triage')}>🤖 QA AI Triage</button>
          <button className="text-button" onClick={() => navigate('/login')}>MPIN Quick Login</button>
          <button className="primary-button" onClick={() => navigate('/signup')}>Sign Up <Icon name="arrow" size={17} /></button>
        </div>
      </header>

      <main>
        <section className="hero-section" id="home">
          <div className="hero-copy">
            <span className="eyebrow">YOUR MONEY, SIMPLIFIED</span>
            <h1>Banking and financial services, <em>built around you.</em></h1>
            <p>Manage your accounts, make secure payments, explore financial products, and plan for your future from one place.</p>
            <div className="hero-buttons">
              <button className="primary-button" onClick={() => navigate('/login')}>MPIN Quick Login</button>
              <button className="secondary-button" onClick={() => navigate('/signup')}>Create an account</button>
            </div>
            <div className="trust-row">
              <span><Icon name="shield" size={18} />Secure Banking</span>
              <span><Icon name="send" size={18} />Digital Payments</span>
              <span><Icon name="chart" size={18} />Financial Planning</span>
            </div>
          </div>

          <div className="hero-visual">
            <div className="purple-orb" />
            <div className="image-arch"><img src="/assets/hero-image.jpeg" alt="Woman using her phone for banking" /></div>
            <div className="hero-preview expense-preview">
              <div className="preview-title"><span className="preview-icon"><Icon name="chart" size={15} /></span><b>Expenses</b></div>
              <div className="donut"><i /><span>Planning<br /><b>view</b></span></div>
              <div className="preview-key"><span><i className="need" /> Essentials</span><span><i className="want" /> Lifestyle</span></div>
            </div>
            <div className="hero-preview investment-preview">
              <div className="preview-title"><span className="preview-icon"><Icon name="chart" size={15} /></span><b>Your investments</b></div>
              <svg className="mini-chart" viewBox="0 0 180 55" aria-hidden="true"><path d="M3 43 C22 18 35 46 52 31 S83 39 99 20 S128 31 145 11 S164 19 177 5" /></svg>
              <div className="preview-caption">Explore your long-term goals <span>↗</span></div>
            </div>
          </div>
        </section>

        <section className="stats">
          <div><strong>One place</strong><span>for your financial life</span></div>
          <div><strong>Built with care</strong><span>for secure digital banking</span></div>
          <div><strong>Clear tools</strong><span>to help you decide</span></div>
        </section>

        <section id="banking">
          <SectionIntro eyebrow="EVERYDAY BANKING" title="Everything you need for everyday banking" text="Manage your money, accounts and everyday financial needs from one secure platform." />
          <div className="service-grid">
            <ServiceCard icon="wallet" title="Savings Account">Manage your savings and access your account information whenever you need it.</ServiceCard>
            <ServiceCard icon="card" title="Account Management" tint="lilac">View account details, manage your profile and keep your banking information up to date.</ServiceCard>
            <ServiceCard icon="chart" title="Transactions" tint="warm">Track money coming into and going out of your accounts with a clear transaction history.</ServiceCard>
          </div>
        </section>

        <section className="payments-section" id="payments">
          <div className="payments-copy">
            <SectionIntro centered={false} eyebrow="UPI & PAYMENTS" title="Send and receive money with ease." text="Make everyday payments and transfers through UPI. Send money, receive payments and keep track of your payment activity from one place." />
            <button className="primary-button">Explore Payments <Icon name="arrow" size={17} /></button>
          </div>
          <div className="payments-grid">
            {payment.map(([i, t], n) => <div className="payment-tile" key={t}><div className={`icon-box tone-${n % 3}`}><Icon name={i} /></div><span>{t}</span><Icon name="arrow" size={17} /></div>)}
          </div>
        </section>

        <section className="loans-section" id="loans">
          <div className="loan-copy">
            <SectionIntro centered={false} eyebrow="LOANS & REPAYMENTS" title="Borrow for the things that matter." text="Explore financing options for different financial needs and understand your repayment obligations before applying." />
            <div className="loan-types">
              {[['wallet', 'Personal Loan'], ['home', 'Home Loan'], ['graduation', 'Education Loan'], ['car', 'Vehicle Loan']].map(([i, t]) => <span key={t}><Icon name={i} size={19} />{t}</span>)}
            </div>
          </div>
          <EmiCalculator />
        </section>

        <section className="insurance-section" id="insurance">
          <div className="insurance-visual">
            <img src="/assets/insurance-family.jpg" alt="Family spending time outdoors" />
            <div className="covered"><Icon name="heart" size={22} /><div><small>Protection that fits</small><b>For every chapter</b></div></div>
          </div>
          <div className="insurance-content">
            <SectionIntro centered={false} eyebrow="INSURANCE" title="Protect what matters to you." text="Explore insurance options designed around your needs and understand the coverage available to you." />
            <div className="coverage-list">{['Health Insurance', 'Life Insurance', 'Vehicle Insurance'].map(x => <span key={x}><Icon name="check" size={18} />{x}</span>)}</div>
            <InsuranceEstimator />
          </div>
        </section>
      </main>

      <footer>
        <div className="footer-top">
          <div className="footer-brand"><a className="logo" href="#home"><i>Y</i>YourBank</a><p>Making everyday banking and financial planning feel simpler, clearer and more personal.</p></div>
          {[['Banking', ['Savings Account', 'Account Management', 'Transactions']], ['Borrow', ['Personal Loans', 'Home Loans', 'Education Loans', 'Vehicle Loans']], ['Protect', ['Health Insurance', 'Life Insurance', 'Vehicle Insurance']], ['Invest', ['Investment Account', 'Holdings', 'Financial Goals']], ['Support', ['Help Center', 'FAQs', 'Contact Support', 'Security']], ['Company', ['About Us', 'Contact', 'Careers']]].map(([t, links]) => <div className="footer-links" key={t}><h4>{t}</h4>{links.map(x => <a href="#home" key={x}>{x}</a>)}</div>)}
        </div>
        <div className="footer-bottom"><span>© 2026 YourBank. All rights reserved.</span><div><a href="#home">Privacy Policy</a><a href="#home">Terms & Conditions</a><a href="#home">Security</a></div></div>
      </footer>
    </div>
  )
}

// Main App Container with OWASP Session Security Management
function AppContent() {
  const navigate = useNavigate()
  const [loggedInUser, setLoggedInUser] = useState(null)

  useEffect(() => {
    const activeSession = sessionStorage.getItem('yourbank_active_session')
    if (activeSession) {
      try {
        setLoggedInUser(JSON.parse(activeSession))
      } catch (err) {
        console.error('Failed to parse active session:', err)
      }
    }
  }, [])

  const handleLogout = () => {
    localStorage.removeItem('yourbank_saved_cif')
    localStorage.removeItem('yourbank_saved_email')
    localStorage.removeItem('yourbank_saved_acc')
    localStorage.clear()
    sessionStorage.removeItem('yourbank_active_session')
    sessionStorage.removeItem('yourbank_last_active_time')
    sessionStorage.clear()
    setLoggedInUser(null)
    navigate('/')
  }

  const handleLoginSuccess = (userSummary) => {
    setLoggedInUser(userSummary)
    navigate('/dashboard/overview')
  }

  return (
    <>
      <Routes>
        <Route path="/" element={<HomePage />} />

        <Route
          path="/signup"
          element={
            <ProtectedRoute requireAuth={false}>
              <SignUpPage onBackToHome={() => navigate('/')} />
            </ProtectedRoute>
          }
        />

        <Route
          path="/login"
          element={
            <ProtectedRoute requireAuth={false}>
              <div className="login-route-page">
                <HomePage />
                <MpinModal
                  isOpen={true}
                  onClose={() => navigate('/')}
                  onSuccessLogin={handleLoginSuccess}
                />
              </div>
            </ProtectedRoute>
          }
        />

        <Route
          path="/dashboard/*"
          element={
            <ProtectedRoute requireAuth={true}>
              <CustomerDashboard user={loggedInUser} onLogout={handleLogout} />
            </ProtectedRoute>
          }
        />

        <Route path="/qa-triage" element={<QaTriageDashboard />} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  )
}
