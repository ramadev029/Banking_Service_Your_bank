import React, { useState } from 'react'

export default function TxnMpinModal({ isOpen, onClose, amount, recipient, onSubmitMpin }) {
  const [mpinDigits, setMpinDigits] = useState(['', '', '', '', '', ''])
  const [activeIdx, setActiveIdx] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

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

  const handleConfirmTransaction = async () => {
    const fullMpin = mpinDigits.join('')
    if (fullMpin.length !== 6) {
      setError('Please enter your complete 6-Digit MPIN.')
      return
    }

    setLoading(true)
    setError(null)

    try {
      await onSubmitMpin(fullMpin)
      handleClear()
      onClose()
    } catch (err) {
      setError(err.message || 'Transaction authorization failed.')
      handleClear()
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card mpin-modal-card" onClick={(e) => e.stopPropagation()}>
        <button className="close-btn" onClick={onClose}>×</button>

        <div className="mpin-modal-header">
          <div className="mpin-badge">SECURITY AUTHORIZATION</div>
          <h2>Confirm Fund Transfer</h2>
          <p>Transferring <b>₹{amount}</b> to <b>{recipient}</b></p>
        </div>

        {error && <div className="error-banner">{error}</div>}

        <div className="mpin-dots-container">
          {mpinDigits.map((digit, i) => (
            <div key={i} className={`mpin-dot ${digit ? 'filled' : ''} ${i === activeIdx ? 'active-dot' : ''}`}>
              {digit ? '•' : ''}
            </div>
          ))}
        </div>

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
          onClick={handleConfirmTransaction}
          disabled={loading || activeIdx !== 6}
          style={{ marginTop: '20px' }}
        >
          {loading ? 'Authorizing Transfer...' : `Confirm & Transfer ₹${amount}`}
        </button>
      </div>
    </div>
  )
}
