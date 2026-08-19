import React from 'react'

export default function VirtualCardVisual({ cardData, holderName, accountNumber }) {
  const formattedCardNumber = cardData?.cardNumber 
    ? cardData.cardNumber.replace(/(\d{4})/g, '$1 ').trim()
    : '4532 •••• •••• 1001'

  const expiry = cardData?.expiryMonth && cardData?.expiryYear
    ? `${String(cardData.expiryMonth).padStart(2, '0')}/${String(cardData.expiryYear).slice(-2)}`
    : '12/31'

  return (
    <div className="virtual-card-wrapper">
      <div className="virtual-card">
        <div className="card-top-row">
          <div className="bank-logo"><i>Y</i> YourBank</div>
          <div className="card-chip">
            <svg width="34" height="26" viewBox="0 0 34 26" fill="none">
              <rect width="34" height="26" rx="4" fill="#E2B867"/>
              <path d="M0 8H34M0 18H34M12 0V26M22 0V26" stroke="#B38A3A" strokeWidth="1"/>
            </svg>
            <span className="contactless">)))</span>
          </div>
        </div>

        <div className="card-number">{formattedCardNumber}</div>

        <div className="card-bottom-row">
          <div className="card-holder">
            <small>CARD HOLDER</small>
            <span>{holderName || 'VALUED CUSTOMER'}</span>
          </div>
          <div className="card-expiry">
            <small>EXPIRES</small>
            <span>{expiry}</span>
          </div>
          <div className="card-network">VISA</div>
        </div>
      </div>
      <div className="card-badge">✨ INSTANT VIRTUAL DEBIT CARD ISSUED</div>
    </div>
  )
}
