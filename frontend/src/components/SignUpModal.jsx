import { useState } from 'react'

export default function SignUpModal({ isOpen, onClose }) {
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    phoneNumber: '',
    panNumber: '',
    aadhaarNumber: '',
    address: '',
    mpin: '984021'
  })

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [successData, setSuccessData] = useState(null)

  if (!isOpen) return null

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
    setError(null)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      const response = await fetch('http://localhost:8080/api/v1/auth/signup', {
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
        throw new Error(data.message || 'Registration failed')
      }

      setSuccessData(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card signup-modal-card" onClick={(e) => e.stopPropagation()}>
        <button className="close-btn" onClick={onClose} aria-label="Close modal">✕</button>
        
        {successData ? (
          <div className="success-box">
            <h3>Account Created Successfully!</h3>
            <p style={{ color: 'var(--muted)', fontSize: '14px' }}>
              Welcome to YourBank, <b>{successData.fullName}</b>. Your instant savings bank account has been provisioned.
            </p>

            <div className="success-details">
              <div><span>CIF Number:</span> <b>{successData.cifNumber}</b></div>
              <div><span>Account Number:</span> <b>{successData.accountNumber}</b></div>
              <div><span>IFSC Code:</span> <b>{successData.ifscCode}</b></div>
              <div><span>Account Type:</span> <b>{successData.accountType}</b></div>
              <div><span>UPI VPA:</span> <b>{successData.upiVpa}</b></div>
              <div><span>Starting Balance:</span> <b>₹{successData.balance}</b></div>
            </div>

            <button className="primary-button full-width" style={{ marginTop: '12px' }} onClick={onClose}>
              Proceed to Login
            </button>
          </div>
        ) : (
          <>
            <div className="modal-header">
              <h2>Open a Savings Account</h2>
              <p className="subtext">Experience seamless, digital banking in seconds.</p>
            </div>

            {error && <div className="error-banner">{error}</div>}

            <form className="modal-form" onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Full Legal Name *</label>
                  <input
                    type="text"
                    name="fullName"
                    placeholder="e.g. Rohan Sharma"
                    value={formData.fullName}
                    onChange={handleChange}
                    required
                  />
                </div>

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
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Phone Number (10 Digits) *</label>
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

                <div className="form-group">
                  <label>Account Password *</label>
                  <input
                    type="password"
                    name="password"
                    placeholder="••••••••"
                    value={formData.password}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>PAN Number (Optional)</label>
                  <input
                    type="text"
                    name="panNumber"
                    placeholder="ABCPE1234F"
                    maxLength={10}
                    value={formData.panNumber}
                    onChange={handleChange}
                  />
                </div>

                <div className="form-group">
                  <label>Aadhaar Number (Optional)</label>
                  <input
                    type="text"
                    name="aadhaarNumber"
                    placeholder="12-digit Aadhaar"
                    maxLength={12}
                    value={formData.aadhaarNumber}
                    onChange={handleChange}
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>6-Digit Security MPIN *</label>
                  <input
                    type="password"
                    name="mpin"
                    placeholder="984021"
                    maxLength={6}
                    value={formData.mpin}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Residential Address</label>
                  <input
                    type="text"
                    name="address"
                    placeholder="Street address, City, State"
                    value={formData.address}
                    onChange={handleChange}
                  />
                </div>
              </div>

              <button type="submit" className="primary-button large-btn full-width" style={{ marginTop: '10px' }} disabled={loading}>
                {loading ? 'Creating Account...' : 'Submit & Open Account'}
              </button>
            </form>
          </>
        )}
      </div>
    </div>
  )
}
