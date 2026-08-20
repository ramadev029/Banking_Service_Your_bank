import React, { useState } from 'react'
import { validateAadhaar, validatePAN, isIndividualPAN, calculateAge } from '../utils/verhoeff'
import VirtualCardVisual from './VirtualCardVisual'

export default function SignUpPage({ onBackToHome }) {
  const [step, setStep] = useState(1)
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phoneNumber: '',
    password: '',
    mpin: '',
    panNumber: '',
    aadhaarNumber: '',
    dateOfBirth: '',
    gender: 'MALE',
    address: '',
    accountType: 'SAVINGS_REGULAR'
  })

  // 2FA Aadhaar OTP state
  const [otpSent, setOtpSent] = useState(false)
  const [otpValue, setOtpValue] = useState('')
  const [otpTxnId, setOtpTxnId] = useState(null)
  const [maskedMobile, setMaskedMobile] = useState('')
  const [otpVerified, setOtpVerified] = useState(false)
  const [otpLoading, setOtpLoading] = useState(false)

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [successResponse, setSuccessResponse] = useState(null)

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'panNumber' ? value.toUpperCase() : value
    }))
    setError(null)
  }

  // Real-time Bank Risk Indicators
  const age = calculateAge(formData.dateOfBirth)
  const isAgeValid = age !== null ? age >= 18 : null
  const isPanFormatValid = formData.panNumber ? validatePAN(formData.panNumber) : null
  const isPanIndiv = formData.panNumber ? isIndividualPAN(formData.panNumber) : null
  const isAadhaarChecksumValid = formData.aadhaarNumber ? validateAadhaar(formData.aadhaarNumber) : null

  // Non-trivial MPIN Check
  const isMpinTrivial = (m) => {
    if (!m || m.length !== 6) return false
    if (/^(.)\1{5}$/.test(m)) return true // Repetitive
    if ('0123456789'.includes(m) || '9876543210'.includes(m)) return true // Sequential
    return false
  }

  const handleNextStep1 = (e) => {
    e.preventDefault()
    if (!formData.fullName || !formData.email || !formData.phoneNumber || !formData.password || !formData.mpin) {
      setError('Please fill in all personal details, password, and 6-digit MPIN.')
      return
    }
    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters long.')
      return
    }
    if (formData.mpin.length !== 6) {
      setError('MPIN must be exactly 6 numeric digits.')
      return
    }
    if (isMpinTrivial(formData.mpin)) {
      setError('Trivial MPIN rejected! Please choose a non-predictable 6-digit MPIN (no 123456 or 111111).')
      return
    }
    setError(null)
    setStep(2)
  }

  const handleSendAadhaarOtp = async () => {
    if (!validateAadhaar(formData.aadhaarNumber)) {
      setError('Please enter a valid 12-digit Aadhaar number first.')
      return
    }
    setOtpLoading(true)
    setError(null)

    try {
      const res = await fetch('http://localhost:8085/api/v1/auth/kyc/send-aadhaar-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ aadhaarNumber: formData.aadhaarNumber })
      })

      const data = await res.json()
      if (!res.ok) throw new Error(data.message || 'Failed to send Aadhaar OTP')

      setOtpTxnId(data.txnId)
      setMaskedMobile(data.maskedMobile)
      setOtpSent(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setOtpLoading(false)
    }
  }

  const handleVerifyAadhaarOtp = async () => {
    if (!otpValue || otpValue.length !== 6) {
      setError('Please enter the 6-digit OTP code.')
      return
    }
    setOtpLoading(true)
    setError(null)

    try {
      const res = await fetch('http://localhost:8085/api/v1/auth/kyc/verify-aadhaar-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ txnId: otpTxnId, otp: otpValue })
      })

      const data = await res.json()
      if (!res.ok) throw new Error(data.message || 'Invalid Aadhaar OTP')

      setOtpVerified(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setOtpLoading(false)
    }
  }

  const handleNextStep2 = (e) => {
    e.preventDefault()
    if (!formData.panNumber || !formData.aadhaarNumber || !formData.dateOfBirth || !formData.address) {
      setError('All regulatory eKYC fields (PAN, Aadhaar, DOB, Address) are mandatory.')
      return
    }
    if (age !== null && age < 18) {
      setError(`Age Restriction: Instant Digital Account is legally restricted to individuals 18+ (Current Age: ${age}).`)
      return
    }
    if (!validatePAN(formData.panNumber)) {
      setError('Invalid PAN format! Must match pattern ABCDE1234F.')
      return
    }
    if (!isIndividualPAN(formData.panNumber)) {
      setError('Non-Individual PAN Detected! Personal Savings Accounts require an Individual PAN (4th letter "P").')
      return
    }
    if (!validateAadhaar(formData.aadhaarNumber)) {
      setError('Invalid Aadhaar number! Verhoeff mathematical checksum failed.')
      return
    }
    if (!otpVerified) {
      setError('Please complete 2-Factor Aadhaar OTP verification before proceeding.')
      return
    }
    setError(null)
    setStep(3)
  }

  const handleSubmitFinal = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      const response = await fetch('http://localhost:8085/api/v1/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      })

      const data = await response.json()

      if (!response.ok) {
        if (data.validationErrors) {
          const firstErr = Object.values(data.validationErrors)[0]
          throw new Error(firstErr)
        }
        throw new Error(data.message || 'Account registration failed.')
      }

      setSuccessResponse(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="onboarding-page-shell">
      <header className="onboarding-header">
        <button className="back-link" onClick={onBackToHome}>
          ← Back to Main Page
        </button>
        <div className="logo"><i>Y</i>YourBank</div>
        <div className="trust-badge-pill">
          256-Bit Bank Grade SSL Encrypted
        </div>
      </header>

      <main className="onboarding-main">
        {successResponse ? (
          /* SUCCESS CELEBRATION DASHBOARD */
          <div className="celebration-card">
            <div className="celebration-badge">CONGRATULATIONS & WELCOME!</div>
            <h2>Your Bank Account is Live & Active</h2>
            <p className="celebration-subtext">
              Welcome to <b>YourBank</b>, {successResponse.fullName}. Your flagship savings account, 6-digit MPIN, and Virtual Debit Card have been successfully provisioned.
            </p>

            <div className="celebration-grid">
              <div className="account-summary-card">
                <h3>Account Summary</h3>
                <div className="summary-row">
                  <span>Customer CIF Number:</span>
                  <b>{successResponse.cifNumber}</b>
                </div>
                <div className="summary-row">
                  <span>Account Number (12-Digit):</span>
                  <b className="highlight-account">{successResponse.accountNumber}</b>
                </div>
                <div className="summary-row">
                  <span>Assigned Branch:</span>
                  <b>{successResponse.branchName}</b>
                </div>
                <div className="summary-row">
                  <span>Branch IFSC Code:</span>
                  <b>{successResponse.ifscCode}</b>
                </div>
                <div className="summary-row">
                  <span>UPI VPA:</span>
                  <b>{successResponse.upiVpa}</b>
                </div>
                <div className="summary-row">
                  <span>6-Digit MPIN:</span>
                  <b style={{ color: '#168444' }}>Secured & Active</b>
                </div>
                <div className="summary-row">
                  <span>Initial Balance:</span>
                  <b>₹{successResponse.balance}</b>
                </div>
              </div>

              <VirtualCardVisual
                cardData={successResponse.debitCard}
                holderName={successResponse.fullName}
                accountNumber={successResponse.accountNumber}
              />
            </div>

            <div className="action-row">
              <button className="primary-button large-btn" onClick={onBackToHome}>
                Proceed to Dashboard Login →
              </button>
            </div>
          </div>
        ) : (
          /* MULTI-STEP SIGNUP FORM WIZARD */
          <div className="onboarding-card">
            <div className="stepper-bar">
              <div className={`step-item ${step >= 1 ? 'active' : ''}`}>
                <span className="step-num">1</span>
                <span className="step-text">Contact & MPIN</span>
              </div>
              <div className="step-line" />
              <div className={`step-item ${step >= 2 ? 'active' : ''}`}>
                <span className="step-num">2</span>
                <span className="step-text">eKYC & 2FA</span>
              </div>
              <div className="step-line" />
              <div className={`step-item ${step >= 3 ? 'active' : ''}`}>
                <span className="step-num">3</span>
                <span className="step-text">Account & Card</span>
              </div>
            </div>

            {error && <div className="error-banner">{error}</div>}

            {/* STEP 1: Personal, Contact & 6-Digit MPIN */}
            {step === 1 && (
              <form onSubmit={handleNextStep1} className="onboarding-form">
                <div className="step-heading">
                  <h2>Personal Details & 6-Digit MPIN</h2>
                  <p>Enter your legal name, contact details, and set your 6-digit MPIN for secure passcode login.</p>
                </div>

                <div className="form-group">
                  <label>Full Legal Name (as per PAN/Aadhaar) *</label>
                  <input
                    type="text"
                    name="fullName"
                    placeholder="e.g. Rohan Sharma"
                    value={formData.fullName}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Email Address *</label>
                    <input
                      type="email"
                      name="email"
                      placeholder="name@example.com"
                      value={formData.email}
                      onChange={handleChange}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Mobile Number (10 Digits) *</label>
                    <input
                      type="tel"
                      name="phoneNumber"
                      placeholder="9876543210"
                      maxLength={10}
                      value={formData.phoneNumber}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Password (Min. 8 characters) *</label>
                    <input
                      type="password"
                      name="password"
                      placeholder="••••••••"
                      value={formData.password}
                      onChange={handleChange}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>
                      Set 6-Digit MPIN (Quick Login Passcode) *
                      {formData.mpin && !isMpinTrivial(formData.mpin) && formData.mpin.length === 6 && (
                        <span className="valid-tag">✓ Non-Trivial MPIN</span>
                      )}
                      {formData.mpin && isMpinTrivial(formData.mpin) && (
                        <span className="invalid-tag">✗ Trivial MPIN Blocked</span>
                      )}
                    </label>
                    <input
                      type="password"
                      name="mpin"
                      placeholder="e.g. 984021"
                      maxLength={6}
                      value={formData.mpin}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>

                <button type="submit" className="primary-button large-btn">
                  Continue to eKYC & 2FA Verification →
                </button>
              </form>
            )}

            {/* STEP 2: Regulatory eKYC & 2FA Aadhaar OTP */}
            {step === 2 && (
              <form onSubmit={handleNextStep2} className="onboarding-form">
                <div className="step-heading">
                  <h2>Bank Regulatory eKYC & 2FA Verification</h2>
                  <p>PAN, Age 18+ check, and 2-Factor Aadhaar OTP verification are required.</p>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Date of Birth *</label>
                    <input
                      type="date"
                      name="dateOfBirth"
                      value={formData.dateOfBirth}
                      onChange={handleChange}
                      required
                    />
                    {isAgeValid === true && <span className="valid-tag">✓ Eligible ({age} Yrs Old)</span>}
                    {isAgeValid === false && <span className="invalid-tag">✗ Minor ({age} Yrs - Blocked)</span>}
                  </div>

                  <div className="form-group">
                    <label>Gender *</label>
                    <select name="gender" value={formData.gender} onChange={handleChange}>
                      <option value="MALE">Male</option>
                      <option value="FEMALE">Female</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label>
                    PAN Number (4th Letter MUST be "P" for Individual) *
                    {isPanFormatValid && isPanIndiv && <span className="valid-tag">✓ Individual Personal PAN</span>}
                    {isPanFormatValid && !isPanIndiv && <span className="invalid-tag">✗ Non-Individual PAN Blocked</span>}
                  </label>
                  <input
                    type="text"
                    name="panNumber"
                    placeholder="ABCPE1234F"
                    maxLength={10}
                    value={formData.panNumber}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>
                    Aadhaar Number (12 Digits) *
                    {isAadhaarChecksumValid === true && <span className="valid-tag">✓ Verhoeff Verified</span>}
                    {isAadhaarChecksumValid === false && <span className="invalid-tag">✗ Checksum Failed</span>}
                  </label>
                  <div className="input-with-button">
                    <input
                      type="text"
                      name="aadhaarNumber"
                      placeholder="234567890123"
                      maxLength={12}
                      value={formData.aadhaarNumber}
                      onChange={handleChange}
                      disabled={otpVerified}
                      required
                    />
                    {!otpVerified && (
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={handleSendAadhaarOtp}
                        disabled={!isAadhaarChecksumValid || otpLoading}
                      >
                        {otpLoading ? 'Sending...' : 'Request 2FA OTP'}
                      </button>
                    )}
                  </div>
                </div>

                {otpSent && !otpVerified && (
                  <div className="otp-box">
                    <label>Enter 6-Digit OTP sent to {maskedMobile} (Sandbox Test OTP: <b>123456</b>)</label>
                    <div className="otp-input-row">
                      <input
                        type="text"
                        placeholder="123456"
                        maxLength={6}
                        value={otpValue}
                        onChange={(e) => setOtpValue(e.target.value)}
                      />
                      <button
                        type="button"
                        className="primary-button"
                        onClick={handleVerifyAadhaarOtp}
                        disabled={otpLoading || otpValue.length !== 6}
                      >
                        {otpLoading ? 'Verifying...' : '✓ Verify OTP'}
                      </button>
                    </div>
                  </div>
                )}

                {otpVerified && (
                  <div className="verified-banner">
                    ✅ 2FA Aadhaar eKYC Verified Successfully!
                  </div>
                )}

                <div className="form-group">
                  <label>Residential Address *</label>
                  <textarea
                    name="address"
                    rows={2}
                    placeholder="Enter street, city, state, pincode"
                    value={formData.address}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="btn-row">
                  <button type="button" className="secondary-button" onClick={() => setStep(1)}>
                    ← Previous
                  </button>
                  <button type="submit" className="primary-button large-btn" disabled={!otpVerified || !isAgeValid || !isPanIndiv}>
                    Continue to Branch & Account →
                  </button>
                </div>
              </form>
            )}

            {/* STEP 3: Branch & Account Selection & Submission */}
            {step === 3 && (
              <form onSubmit={handleSubmitFinal} className="onboarding-form">
                <div className="step-heading">
                  <h2>Account Scheme & Branch Confirmation</h2>
                  <p>Confirm your account tier and branch allocation for <b>YourBank</b>.</p>
                </div>

                <div className="branch-info-box">
                  <div className="branch-icon">🏛️</div>
                  <div>
                    <h4>Assigned Flagship Branch: YourBank Main Digital Branch</h4>
                    <p>Branch Code: <b>0001</b> | IFSC Code: <b>YBRK0000001</b> | Bengaluru HQ</p>
                  </div>
                </div>

                <div className="form-group">
                  <label>Savings Account Type *</label>
                  <select name="accountType" value={formData.accountType} onChange={handleChange}>
                    <option value="SAVINGS_REGULAR">Regular Savings Account (Free UPI & Instant Card)</option>
                    <option value="SAVINGS_SALARY">Zero-Balance Salary Account</option>
                    <option value="SAVINGS_PREMIUM">Premium Digital Savings (High Daily Limits)</option>
                  </select>
                </div>

                <div className="assurance-box">
                  <span>DICGC Deposit Insurance Coverage up to ₹5,00,000 per customer.</span>
                </div>

                <div className="btn-row">
                  <button type="button" className="secondary-button" onClick={() => setStep(2)}>
                    ← Previous
                  </button>
                  <button type="submit" className="primary-button large-btn" disabled={loading}>
                    {loading ? 'Provisioning Account & Card...' : 'Finalize & Issue Account'}
                  </button>
                </div>
              </form>
            )}
          </div>
        )}
      </main>
    </div>
  )
}
