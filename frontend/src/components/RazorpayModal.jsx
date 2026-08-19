import React, { useState } from 'react'

export default function RazorpayModal({ isOpen, onClose, amount, accountNumber, customerName, onSuccess }) {
  const [selectedMethod, setSelectedMethod] = useState('upi')
  const [selectedBank, setSelectedBank] = useState('HDFC Bank')
  const [cardNumber, setCardNumber] = useState('4111 2222 3333 4444')
  const [cardExpiry, setCardExpiry] = useState('12/28')
  const [cardCvv, setCardCvv] = useState('888')
  const [upiVpa, setUpiVpa] = useState('customer@okaxis')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  if (!isOpen) return null

  const handleCompleteDeposit = async () => {
    setLoading(true)
    setError(null)

    try {
      const paymentId = 'pay_rzp_' + Math.random().toString(36).substring(2, 10)
      const orderId = 'order_rzp_' + Math.random().toString(36).substring(2, 10)
      const signature = 'sig_rzp_' + Math.random().toString(36).substring(2, 10)

      let remarkDetails = ''
      let categoryTag = 'RAZORPAY_GATEWAY'

      if (selectedMethod === 'upi') {
        categoryTag = 'RAZORPAY_UPI'
        remarkDetails = `Deposit of ₹${amount} via Razorpay UPI (${upiVpa || 'GPay/PhonePe'})`
      } else if (selectedMethod === 'card') {
        categoryTag = 'RAZORPAY_CARD'
        const maskedCard = cardNumber.length >= 4 ? `•••• ${cardNumber.slice(-4)}` : '•••• 4444'
        remarkDetails = `Deposit of ₹${amount} via Razorpay External Card (${maskedCard})`
      } else if (selectedMethod === 'netbanking') {
        categoryTag = 'RAZORPAY_NETBANKING'
        remarkDetails = `Deposit of ₹${amount} via Razorpay NetBanking (${selectedBank})`
      }

      const verifyRes = await fetch(`http://localhost:8080/api/v1/payments/razorpay/verify?amount=${amount}&category=${categoryTag}&remarks=${encodeURIComponent(remarkDetails)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          accountNumber,
          razorpayPaymentId: paymentId,
          razorpayOrderId: orderId,
          razorpaySignature: signature
        })
      })

      const verifyData = await verifyRes.json()
      if (!verifyRes.ok) throw new Error(verifyData.message || 'Razorpay payment verification failed.')

      onSuccess(verifyData)
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card rzp-sandbox-modal" onClick={(e) => e.stopPropagation()}>
        <button className="close-btn" onClick={onClose}>×</button>

        <div className="rzp-modal-header">
          <div className="rzp-brand-badge">💳 RAZORPAY PAYMENT GATEWAY</div>
          <h3>Add ₹{amount}.00 to YourBank</h3>
          <p>Destination Account: <b>{accountNumber}</b> ({customerName})</p>
        </div>

        {error && <div className="error-banner">{error}</div>}

        <div className="payment-method-selector">
          <button
            className={`method-tile ${selectedMethod === 'upi' ? 'active' : ''}`}
            onClick={() => setSelectedMethod('upi')}
          >
            📲 UPI / QR (GPay, PhonePe, Paytm, BHIM)
          </button>
          <button
            className={`method-tile ${selectedMethod === 'card' ? 'active' : ''}`}
            onClick={() => setSelectedMethod('card')}
          >
            💳 External Credit / Debit Card
          </button>
          <button
            className={`method-tile ${selectedMethod === 'netbanking' ? 'active' : ''}`}
            onClick={() => setSelectedMethod('netbanking')}
          >
            🏛️ NetBanking (Other Banks ➔ YourBank)
          </button>
        </div>

        {/* METHOD 1: UPI / QR CODE */}
        {selectedMethod === 'upi' && (
          <div className="method-detail-box">
            <div className="form-group" style={{ marginBottom: '10px' }}>
              <label>Enter Virtual Payment Address (VPA)</label>
              <input
                type="text"
                placeholder="e.g. name@okaxis or user@ybl"
                value={upiVpa}
                onChange={(e) => setUpiVpa(e.target.value)}
                className="rzp-input"
              />
            </div>
            <div className="qr-placeholder" style={{ textAlign: 'center', marginTop: '10px' }}>
              <div className="dummy-qr">📱 Scan & Deposit via QR Code</div>
              <p>Supports Google Pay, PhonePe, Paytm, CRED & BHIM</p>
            </div>
          </div>
        )}

        {/* METHOD 2: EXTERNAL CREDIT / DEBIT CARD */}
        {selectedMethod === 'card' && (
          <div className="method-detail-box">
            <p className="subtext" style={{ margin: '0 0 10px', fontSize: '12px' }}>
              Use any Visa, Mastercard, or RuPay card to top-up your account balance.
            </p>
            <div className="form-group">
              <label>16-Digit Card Number</label>
              <input
                type="text"
                placeholder="4111 2222 3333 4444"
                value={cardNumber}
                onChange={(e) => setCardNumber(e.target.value)}
                className="rzp-input"
              />
            </div>
            <div className="form-row" style={{ display: 'flex', gap: '10px' }}>
              <div className="form-group" style={{ flex: 1 }}>
                <label>Expiry (MM/YY)</label>
                <input
                  type="text"
                  placeholder="12/28"
                  value={cardExpiry}
                  onChange={(e) => setCardExpiry(e.target.value)}
                  className="rzp-input"
                />
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                <label>CVV Security Code</label>
                <input
                  type="password"
                  maxLength="4"
                  placeholder="888"
                  value={cardCvv}
                  onChange={(e) => setCardCvv(e.target.value)}
                  className="rzp-input"
                />
              </div>
            </div>
          </div>
        )}

        {/* METHOD 3: NETBANKING */}
        {selectedMethod === 'netbanking' && (
          <div className="method-detail-box">
            <p className="subtext" style={{ margin: '0 0 10px', fontSize: '12px' }}>
              Transfer money directly from another bank into your YourBank account.
            </p>
            <div className="form-group">
              <label>Select Source Bank</label>
              <select
                value={selectedBank}
                onChange={(e) => setSelectedBank(e.target.value)}
                className="rzp-input"
              >
                <option value="HDFC Bank">HDFC Bank NetBanking</option>
                <option value="State Bank of India">State Bank of India (SBI)</option>
                <option value="ICICI Bank">ICICI Bank NetBanking</option>
                <option value="Axis Bank">Axis Bank NetBanking</option>
                <option value="Kotak Mahindra Bank">Kotak Mahindra Bank</option>
                <option value="Punjab National Bank">Punjab National Bank (PNB)</option>
              </select>
            </div>
          </div>
        )}

        <button
          className="primary-button rzp-pay-btn large-btn full-width"
          onClick={handleCompleteDeposit}
          disabled={loading}
        >
          {loading ? 'Processing Deposit...' : `🔐 Deposit ₹${amount}.00 into YourBank Account`}
        </button>
      </div>
    </div>
  )
}
