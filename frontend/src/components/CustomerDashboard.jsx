import React, { useState, useEffect } from 'react'
import { useNavigate, useLocation, Navigate } from 'react-router-dom'
import VirtualCardVisual from './VirtualCardVisual'
import RazorpayModal from './RazorpayModal'
import TxnMpinModal from './TxnMpinModal'
import UpiSuccessModal from './UpiSuccessModal'

export default function CustomerDashboard({ user, onLogout }) {
  const navigate = useNavigate()
  const location = useLocation()

  // Initialize from user prop or ephemeral sessionStorage (OWASP Compliant)
  const [dashboardData, setDashboardData] = useState(() => {
    if (user) return user
    const saved = sessionStorage.getItem('yourbank_active_session')
    if (saved) {
      try {
        return JSON.parse(saved)
      } catch (e) {
        return null
      }
    }
    return null
  })

  const [showBalance, setShowBalance] = useState(true)

  // Sync sub-tab from current URL pathname (/dashboard/overview, /dashboard/payments, etc.)
  const currentTab = location.pathname.split('/')[2] || 'overview'

  // Payment states
  const [transferMode, setTransferMode] = useState('UPI') // UPI, IMPS, NETBANKING
  const [upiRecipient, setUpiRecipient] = useState('')
  const [ifscCode, setIfscCode] = useState('YBNK0000001')
  const [beneficiaryName, setBeneficiaryName] = useState('')
  const [upiAmount, setUpiAmount] = useState('')
  const [upiRemarks, setUpiRemarks] = useState('')
  const [paymentLoading, setPaymentLoading] = useState(false)
  const [paymentSuccess, setPaymentSuccess] = useState(null)
  const [paymentError, setPaymentError] = useState(null)

  // Receipt & Modal states
  const [razorpayAmount, setRazorpayAmount] = useState('1000')
  const [showRazorpayModal, setShowRazorpayModal] = useState(false)
  const [showTxnMpinModal, setShowTxnMpinModal] = useState(false)
  const [successReceiptData, setSuccessReceiptData] = useState(null)
  const [showReceiptModal, setShowReceiptModal] = useState(false)
  const [isDepositReceipt, setIsDepositReceipt] = useState(false)

  // OWASP Visibility & Inactivity Security Guard
  useEffect(() => {
    // 1. Trap browser Back button
    window.history.pushState(null, '', window.location.href)
    const handlePopState = () => {
      window.history.pushState(null, '', window.location.href)
    }
    window.addEventListener('popstate', handlePopState)

    // 2. Tab visibility & Inactivity listener (Visibilitychange)
    const handleVisibilityChange = () => {
      if (document.hidden) {
        const leaveTime = Date.now()
        sessionStorage.setItem('yourbank_leave_time', leaveTime.toString())
      } else {
        const leaveTime = parseInt(sessionStorage.getItem('yourbank_leave_time') || '0')
        if (leaveTime > 0 && Date.now() - leaveTime > 60000) { // 60s inactivity
          sessionStorage.removeItem('yourbank_active_session')
          alert('Session Expired for Security (OWASP Inactivity Policy). Please log in again.')
          onLogout()
          navigate('/')
        }
      }
    }

    document.addEventListener('visibilitychange', handleVisibilityChange)

    return () => {
      window.removeEventListener('popstate', handlePopState)
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
  }, [navigate, onLogout])

  // Auto-fetch latest live dashboard statements & credits on mount and tab navigation
  useEffect(() => {
    if (dashboardData && (dashboardData.cifNumber || dashboardData.email)) {
      fetchLatestDashboard()
    }
  }, [location.pathname])

  if (!dashboardData) {
    return <Navigate to="/" replace />
  }

  const fetchLatestDashboard = async () => {
    const idToFetch = dashboardData.cifNumber || dashboardData.email
    if (!idToFetch) return
    try {
      const res = await fetch(`http://localhost:8085/api/v1/customer/dashboard?cifNumber=${idToFetch}`)
      if (res.ok) {
        const updated = await res.json()
        setDashboardData(updated)
        sessionStorage.setItem('yourbank_active_session', JSON.stringify(updated))
      }
    } catch (e) {
      console.error('Dashboard refresh failed:', e)
    }
  }

  const handleTabChange = (tabName) => {
    sessionStorage.setItem('yourbank_last_active_time', Date.now().toString())
    navigate(`/dashboard/${tabName}`)
  }

  const handleUpiPayment = (e) => {
    e.preventDefault()
    if (!upiRecipient || !upiAmount) {
      setPaymentError('Please enter recipient VPA/Account and payment amount.')
      return
    }
    setPaymentError(null)
    setPaymentSuccess(null)
    setShowTxnMpinModal(true)
  }

  const executeUpiTransfer = async (mpin) => {
    setPaymentLoading(true)
    setPaymentError(null)

    try {
      const res = await fetch('http://localhost:8085/api/v1/payments/upi', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          accountNumber: dashboardData.accountNumber,
          recipient: upiRecipient.trim(),
          amount: parseFloat(upiAmount),
          remarks: upiRemarks,
          transferMode,
          ifscCode,
          beneficiaryName,
          mpin
        })
      })

      const data = await res.json()
      if (!res.ok) throw new Error(data.message || 'Payment transfer failed.')

      setIsDepositReceipt(false)
      setSuccessReceiptData(data)
      setShowReceiptModal(true)
      setPaymentSuccess(`${transferMode} Transfer of ₹${data.amount} to ${upiRecipient} completed. (Txn ID: ${data.transactionId})`)
      setUpiRecipient('')
      setUpiAmount('')
      setUpiRemarks('')
      setBeneficiaryName('')
      fetchLatestDashboard()
    } catch (err) {
      setPaymentError(err.message)
      throw err
    } finally {
      setPaymentLoading(false)
    }
  }

  const handleRazorpayAddFunds = () => {
    setPaymentError(null)
    setPaymentSuccess(null)
    setShowRazorpayModal(true)
  }

  const handleRazorpaySuccess = (verifyData) => {
    setIsDepositReceipt(true)
    setSuccessReceiptData(verifyData)
    setShowReceiptModal(true)
    setPaymentSuccess(`Added ₹${verifyData.amount} via Deposit Gateway. New Balance: ₹${verifyData.balanceAfter}`)
    fetchLatestDashboard()
  }

  const formatCurrency = (val) => {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(val || 0)
  }

  return (
    <div className="dashboard-shell">
      {/* HEADER & BRANDING */}
      <header className="dashboard-header">
        <div className="logo">
          YourBank <span className="brand-tag">ENTERPRISE</span>
        </div>

        <nav className="header-nav-pills">
          <button className={`nav-pill ${currentTab === 'overview' ? 'active' : ''}`} onClick={() => handleTabChange('overview')}>
            Overview
          </button>
          <button className={`nav-pill ${currentTab === 'payments' ? 'active' : ''}`} onClick={() => handleTabChange('payments')}>
            UPI & Fund Transfer
          </button>
          <button className={`nav-pill ${currentTab === 'history' ? 'active' : ''}`} onClick={() => handleTabChange('history')}>
            Transactions
          </button>
          <button className={`nav-pill ${currentTab === 'loans' ? 'active' : ''}`} onClick={() => handleTabChange('loans')}>
            Loans & Credit
          </button>
          <button className={`nav-pill ${currentTab === 'insurance' ? 'active' : ''}`} onClick={() => handleTabChange('insurance')}>
            Insurance
          </button>
          <button className={`nav-pill ${currentTab === 'support' ? 'active' : ''}`} onClick={() => handleTabChange('support')}>
            Help & Support
          </button>
        </nav>

        <div className="profile-widget">
          <div className="profile-text">
            <b>{dashboardData.fullName}</b>
            <span className="cif-tag">CIF: {dashboardData.cifNumber}</span>
          </div>
          <button className="logout-btn" onClick={onLogout} title="Log Out & Invalidate Session">
            Logout
          </button>
        </div>
      </header>

      {/* DASHBOARD CONTENT BODY */}
      <main className="dashboard-main">
        {/* SECTION 1: ACCOUNT OVERVIEW & VIRTUAL DEBIT CARD */}
        {currentTab === 'overview' && (
          <div className="tab-section">
            <div className="hero-dashboard-grid">
              {/* Profile Summary Card */}
              <div className="dash-card profile-summary-card">
                <span className="badge-cif">VERIFIED CUSTOMER</span>
                <h2>Welcome Back, {dashboardData.fullName}</h2>
                <p className="subtext">Manage your savings, transfers, digital cards, and transaction statements.</p>

                <div className="account-details-grid">
                  <div className="acc-row">
                    <span>Account Number</span>
                    <b>{dashboardData.accountNumber}</b>
                  </div>
                  <div className="acc-row">
                    <span>IFSC Code</span>
                    <b>{dashboardData.ifscCode}</b>
                  </div>
                  <div className="acc-row">
                    <span>UPI VPA</span>
                    <b style={{ color: 'var(--purple)' }}>{dashboardData.upiVpa}</b>
                  </div>
                  <div className="acc-row">
                    <span>Branch Name</span>
                    <b>{dashboardData.branchName} ({dashboardData.city})</b>
                  </div>
                  <div className="acc-row">
                    <span>Registered Phone</span>
                    <b>+91 {dashboardData.phoneNumber || '9876543210'}</b>
                  </div>
                </div>
              </div>

              {/* Account Balance Widget */}
              <div className="dash-card balance-card">
                <div className="balance-top">
                  <span className="balance-label">Primary Savings Balance</span>
                  <button className="toggle-eye-btn" onClick={() => setShowBalance(!showBalance)}>
                    {showBalance ? 'Hide Balance' : 'Show Balance'}
                  </button>
                </div>

                <div className="balance-val">
                  {showBalance ? (
                    <span>{formatCurrency(dashboardData.balance)}</span>
                  ) : (
                    <span>••••••••</span>
                  )}
                </div>

                <div className="balance-footer">
                  <div className="card-badge-row">
                    <span className="badge-pill green">KYC {dashboardData.kycStatus || 'VERIFIED'}</span>
                    <span className="badge-pill purple">{dashboardData.accountType || 'SAVINGS'} ACCOUNT</span>
                  </div>
                </div>
              </div>

              {/* Interactive Virtual Debit Card */}
              <div className="dash-card card-visual-wrapper">
                <h3 style={{ margin: '0 0 12px', fontSize: '16px' }}>Digital Debit Card</h3>
                <VirtualCardVisual
                  cardNumber={dashboardData.debitCardNumber || '4000 0100 0001 0001'}
                  cardHolder={dashboardData.fullName}
                  expiryMonth={dashboardData.debitCardExpiryMonth || '12'}
                  expiryYear={dashboardData.debitCardExpiryYear || '28'}
                  cvv={dashboardData.debitCardCvv || '888'}
                  cardStatus={dashboardData.debitCardStatus || 'ACTIVE'}
                />
              </div>
            </div>
          </div>
        )}

        {/* SECTION 2: MULTI-METHOD FUND TRANSFERS & RAZORPAY */}
        {currentTab === 'payments' && (
          <div className="tab-section">
            <div className="payments-dashboard-grid">
              <div className="dash-card">
                <h3>Transfer Funds & Send Money</h3>
                <p className="subtext">Select your transfer mode below to initiate instant secure bank transfers.</p>

                {paymentSuccess && <div className="success-banner">{paymentSuccess}</div>}
                {paymentError && <div className="error-banner">{paymentError}</div>}

                {/* Subnav for Transfer Methods */}
                <div className="transfer-method-subnav">
                  <button
                    type="button"
                    className={`subnav-btn ${transferMode === 'UPI' ? 'active' : ''}`}
                    onClick={() => setTransferMode('UPI')}
                  >
                    Instant UPI VPA
                  </button>
                  <button
                    type="button"
                    className={`subnav-btn ${transferMode === 'IMPS' ? 'active' : ''}`}
                    onClick={() => setTransferMode('IMPS')}
                  >
                    IMPS / NEFT Transfer
                  </button>
                  <button
                    type="button"
                    className={`subnav-btn ${transferMode === 'NETBANKING' ? 'active' : ''}`}
                    onClick={() => setTransferMode('NETBANKING')}
                  >
                    NetBanking Direct
                  </button>
                </div>

                <form onSubmit={handleUpiPayment} className="dash-form">
                  {transferMode === 'IMPS' && (
                    <div className="form-group">
                      <label>Beneficiary Full Name *</label>
                      <input
                        type="text"
                        placeholder="e.g. Rohan Sharma"
                        value={beneficiaryName}
                        onChange={(e) => setBeneficiaryName(e.target.value)}
                        required
                      />
                    </div>
                  )}

                  <div className="form-group">
                    <label>
                      {transferMode === 'UPI' ? 'Recipient UPI VPA or Account Number *' : 'Recipient Account Number *'}
                    </label>
                    <input
                      type="text"
                      placeholder={transferMode === 'UPI' ? 'e.g. rohan@ybank or 000101100002' : 'e.g. 000101100002'}
                      value={upiRecipient}
                      onChange={(e) => setUpiRecipient(e.target.value)}
                      required
                    />
                  </div>

                  {transferMode === 'IMPS' && (
                    <div className="form-group">
                      <label>IFSC Code *</label>
                      <input
                        type="text"
                        placeholder="e.g. YBNK0000001"
                        value={ifscCode}
                        onChange={(e) => setIfscCode(e.target.value)}
                        required
                      />
                    </div>
                  )}

                  <div className="form-group">
                    <label>Amount (₹) *</label>
                    <input
                      type="number"
                      placeholder="1000"
                      min="1"
                      value={upiAmount}
                      onChange={(e) => setUpiAmount(e.target.value)}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Remarks / Notes</label>
                    <input
                      type="text"
                      placeholder="e.g. Rent, Groceries, Invoice"
                      value={upiRemarks}
                      onChange={(e) => setUpiRemarks(e.target.value)}
                    />
                  </div>

                  <button type="submit" className="primary-button large-btn full-width" disabled={paymentLoading}>
                    {paymentLoading ? 'Processing Transfer...' : `Initiate ${transferMode} Transfer`}
                  </button>
                </form>
              </div>

              {/* Razorpay Top-up Card */}
              <div className="dash-card razorpay-card">
                <div className="rzp-header">
                  <span className="rzp-badge">SELF DEPOSIT GATEWAY</span>
                  <h3>Deposit Funds to Your Account</h3>
                  <p className="subtext">Top-up your primary savings balance securely using Razorpay Gateway integration.</p>
                </div>

                <div className="form-group">
                  <label>Select Deposit Amount (₹)</label>
                  <select value={razorpayAmount} onChange={(e) => setRazorpayAmount(e.target.value)}>
                    <option value="500">₹500.00</option>
                    <option value="1000">₹1,000.00</option>
                    <option value="5000">₹5,000.00</option>
                    <option value="10000">₹10,000.00</option>
                  </select>
                </div>

                <button className="primary-button rzp-btn large-btn full-width" onClick={handleRazorpayAddFunds} disabled={paymentLoading}>
                  {paymentLoading ? 'Connecting Deposit Gateway...' : `Deposit ₹${parseFloat(razorpayAmount).toLocaleString('en-IN')} to Account`}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* SECTION 3: REAL-TIME TRANSACTION HISTORY */}
        {currentTab === 'history' && (
          <div className="tab-section">
            <div className="dash-card">
              <div className="card-header-row" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <div>
                  <h3 style={{ margin: 0 }}>Real-Time Transaction Ledger</h3>
                  <p className="subtext" style={{ margin: 0 }}>Comprehensive record of incoming credits, UPI debits, and self-deposits.</p>
                </div>
                <button className="secondary-button" onClick={fetchLatestDashboard}>Refresh Statements</button>
              </div>

              <div className="table-responsive">
                <table className="txn-table">
                  <thead>
                    <tr>
                      <th>Txn ID</th>
                      <th>Type</th>
                      <th>Category</th>
                      <th>Amount (₹)</th>
                      <th>Balance After</th>
                      <th>Creditor / Debtor Info</th>
                      <th>Remarks</th>
                      <th>Date & Time</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dashboardData.transactions && dashboardData.transactions.length > 0 ? (
                      dashboardData.transactions.map((t) => (
                        <tr key={t.transactionId}>
                          <td className="mono-font bold-text">{t.transactionId}</td>
                          <td>
                            <span className={`txn-badge ${t.type === 'CREDIT' ? 'credit' : 'debit'}`}>
                              {t.type === 'CREDIT' ? 'CREDIT' : 'DEBIT'}
                            </span>
                          </td>
                          <td className="mono-font">{t.category}</td>
                          <td className={`amount-cell ${t.type === 'CREDIT' ? 'credit-text' : 'debit-text'}`}>
                            {t.type === 'CREDIT' ? '+' : '-'}{formatCurrency(t.amount)}
                          </td>
                          <td className="bold-text">{formatCurrency(t.balanceAfter)}</td>
                          <td className="small-text">
                            {t.type === 'CREDIT' ? (
                              <div>
                                <b>From: {t.senderName || 'External Deposit'}</b>
                                <div style={{ fontSize: '11px', color: 'var(--muted)' }}>{t.senderVpa}</div>
                              </div>
                            ) : (
                              <div>
                                <b>To: {t.receiverName || 'Recipient'}</b>
                                <div style={{ fontSize: '11px', color: 'var(--muted)' }}>{t.receiverVpa}</div>
                              </div>
                            )}
                          </td>
                          <td className="small-text muted-text">{t.remarks}</td>
                          <td className="date-cell">{new Date(t.createdAt).toLocaleString('en-IN')}</td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="8" className="empty-state">No transactions recorded yet.</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* SECTION 4: LOANS */}
        {currentTab === 'loans' && (
          <div className="tab-section">
            <div className="dash-card">
              <h3>Instant Personal & Home Loans</h3>
              <p className="subtext">Pre-approved credit lines tailored for your profile.</p>

              <div className="loans-grid">
                <div className="loan-product-card">
                  <h4>Instant Personal Loan</h4>
                  <p className="rate">Interest Rate: 10.5% p.a.</p>
                  <p>Up to ₹5,000,000 with flexible tenure from 12 to 60 months.</p>
                </div>
                <div className="loan-product-card">
                  <h4>Smart Home Loan</h4>
                  <p className="rate">Interest Rate: 8.4% p.a.</p>
                  <p>Turnkey home financing with instant digital Sanction Letter.</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* SECTION 5: INSURANCE */}
        {currentTab === 'insurance' && (
          <div className="tab-section">
            <div className="dash-card">
              <h3>Life & Health Insurance Shield</h3>
              <p className="subtext">Comprehensive protection plans powered by YourBank Assurance.</p>

              <div className="insurance-plans-grid">
                <div className="plan-card">
                  <h4>Health Shield Super Top-Up</h4>
                  <p className="rate">Coverage: ₹1,000,000 | Premium: ₹499/mo</p>
                </div>
                <div className="plan-card">
                  <h4>Term Life Protect Plus</h4>
                  <p className="rate">Coverage: ₹5,000,000 | Premium: ₹799/mo</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* SECTION 6: SUPPORT & SECURITY CENTER */}
        {currentTab === 'support' && (
          <div className="tab-section">
            <div className="dash-card">
              <h3>24x7 Customer Support & Security Center</h3>
              <p className="subtext">Have questions or need assistance? Reach out to your assigned flagship branch.</p>

              <div className="support-info-grid">
                <div className="support-tile">
                  <h4>Toll-Free Helpline</h4>
                  <p>1800-123-YOURBANK (1800-123-9687)</p>
                </div>
                <div className="support-tile">
                  <h4>Email Support</h4>
                  <p>support@yourbank.com</p>
                </div>
                <div className="support-tile">
                  <h4>Flagship Branch</h4>
                  <p>YourBank Main Digital Branch, Bengaluru HQ</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* INTERACTIVE RAZORPAY GATEWAY MODAL */}
        <RazorpayModal
          isOpen={showRazorpayModal}
          onClose={() => setShowRazorpayModal(false)}
          amount={razorpayAmount}
          accountNumber={dashboardData?.accountNumber || ''}
          customerName={dashboardData?.fullName || ''}
          onSuccess={handleRazorpaySuccess}
        />

        {/* OWASP TRANSACTION MPIN AUTHORIZATION MODAL */}
        <TxnMpinModal
          isOpen={showTxnMpinModal}
          onClose={() => setShowTxnMpinModal(false)}
          amount={upiAmount}
          recipient={upiRecipient}
          onSubmitMpin={executeUpiTransfer}
        />

        {/* ANIMATED PAYMENT / DEPOSIT ACKNOWLEDGEMENT MODAL */}
        <UpiSuccessModal
          isOpen={showReceiptModal}
          onClose={() => setShowReceiptModal(false)}
          txnData={successReceiptData}
          recipient={upiRecipient || dashboardData?.fullName || 'Your Savings Account'}
          amount={isDepositReceipt ? razorpayAmount : upiAmount || successReceiptData?.amount}
          mode={isDepositReceipt ? 'Razorpay Self-Deposit' : transferMode}
          isDeposit={isDepositReceipt}
        />
      </main>
    </div>
  )
}
