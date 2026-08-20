import React, { useState, useEffect } from 'react'

export default function MpinModal({ isOpen, onClose, onSuccessLogin }) {
  const [identifier, setIdentifier] = useState('')
  const [mpinDigits, setMpinDigits] = useState(['', '', '', '', '', ''])
  const [activeIdx, setActiveIdx] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (isOpen) {
      setIdentifier('')
      setMpinDigits(['', '', '', '', '', ''])
      setActiveIdx(0)
      setError(null)
    }
  }, [isOpen])

  if (!isOpen) return null

  const handleKeyPress = (num) => {
    if (activeIdx < 6) {
      const newDigits = [...mpinDigits]
      newDigits[activeIdx] = String(num)
      setMpinDigits(newDigits)
      setActiveIdx(activeIdx + 1)
      setError(null)
    }
  }

  const handleDelete = () => {
    if (activeIdx > 0) {
      const newDigits = [...mpinDigits]
      newDigits[activeIdx - 1] = ''
      setMpinDigits(newDigits)
      setActiveIdx(activeIdx - 1)
      setError(null)
    }
  }

  const handleClear = () => {
    setMpinDigits(['', '', '', '', '', ''])
    setActiveIdx(0)
    setError(null)
  }

  const handleSubmitMpin = async (e) => {
    if (e) e.preventDefault()
    const fullMpin = mpinDigits.join('')

    if (!identifier || !identifier.trim()) {
      setError('Please enter your Phone Number, Email, Customer CIF ID, or Account Number.')
      return
    }
    if (fullMpin.length !== 6) {
      setError('Please enter your complete 6-Digit MPIN.')
      return
    }

    setLoading(true)
    setError(null)

    try {
      const response = await fetch('http://localhost:8085/api/v1/auth/mpin/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ identifier: identifier.trim(), mpin: fullMpin })
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.message || 'MPIN authentication failed.')
      }

      // Ephemeral active session ONLY in sessionStorage (OWASP compliant)
      sessionStorage.setItem('yourbank_active_session', JSON.stringify(data))
      sessionStorage.setItem('yourbank_last_active_time', Date.now().toString())

      onSuccessLogin(data)
      onClose()
    } catch (err) {
      setError(err.message)
      handleClear()
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card mpin-modal-card" onClick={(e) => e.stopPropagation()}>
        <button className="close-btn" onClick={onClose} aria-label="Close">✕</button>

        <div className="mpin-modal-header">
          <div className="mpin-badge">INSTANT MPIN LOGIN</div>
          <h2>Enter 6-Digit MPIN</h2>
          <p>Login to <b>YourBank Enterprise</b> securely with your credentials.</p>
        </div>

        {error && <div className="error-banner">{error}</div>}

        <div className="form-group" style={{ marginBottom: '16px', textAlign: 'left' }}>
          <label>Customer CIF ID / Account No / Email *</label>
          <input
            type="text"
            placeholder="e.g. 000101100001 or email@example.com"
            value={identifier}
            onChange={(e) => setIdentifier(e.target.value)}
            required
            autoFocus
          />
        </div>

        {/* 6-Circle Passcode Dots */}
        <div className="mpin-dots-container">
          {mpinDigits.map((digit, i) => (
            <div key={i} className={`mpin-dot ${digit ? 'filled' : ''} ${i === activeIdx ? 'active-dot' : ''}`}>
              {digit ? '•' : ''}
            </div>
          ))}
        </div>

        {/* Interactive Numeric Keypad */}
        <div className="numeric-keypad">
          {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((n) => (
            <button key={n} type="button" className="key-btn" onClick={() => handleKeyPress(n)}>
              {n}
            </button>
          ))}
          <button type="button" className="key-btn action-key" onClick={handleClear}>CLR</button>
          <button type="button" className="key-btn" onClick={() => handleKeyPress(0)}>0</button>
          <button type="button" className="key-btn action-key" onClick={handleDelete}>⌫</button>
        </div>

        <button
          type="button"
          className="primary-button large-btn full-width"
          onClick={handleSubmitMpin}
          disabled={loading || activeIdx !== 6 || !identifier}
          style={{ marginTop: '20px' }}
        >
          {loading ? 'Authenticating MPIN...' : 'Unlock Bank Dashboard'}
        </button>
      </div>
    </div>
  )
}
