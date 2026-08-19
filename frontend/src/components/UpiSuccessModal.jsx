import React from 'react'

export default function UpiSuccessModal({ isOpen, onClose, txnData, recipient, amount, mode, isDeposit }) {
  if (!isOpen || !txnData) return null

  const formattedDate = new Date(txnData.createdAt || Date.now()).toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: true
  })

  const isDepositMode = isDeposit || mode === 'DEPOSIT' || (txnData.type === 'CREDIT' && !txnData.senderVpa?.includes('@ybank'))

  const receiverDisplayName = isDepositMode
    ? 'Your Savings Account'
    : (txnData.receiverName && txnData.receiverName !== recipient
        ? `${txnData.receiverName}`
        : recipient)

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card upi-success-modal" onClick={(e) => e.stopPropagation()}>
        <button className="close-btn" onClick={onClose}>×</button>

        {/* Animated Green Circle with Checkmark */}
        <div className="animated-success-icon-wrap">
          <div className="success-checkmark-circle">
            <svg className="checkmark-svg" viewBox="0 0 52 52">
              <circle className="checkmark-circle-line" cx="26" cy="26" r="23" fill="none" />
              <path className="checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
            </svg>
          </div>
        </div>

        <div className="upi-success-header">
          <div className="success-badge">
            {isDepositMode ? 'DEPOSIT ACKNOWLEDGEMENT' : 'PAYMENT ACKNOWLEDGEMENT'}
          </div>
          <h2 className="success-amount">
            ₹{parseFloat(amount || txnData.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </h2>
          <p className="success-subtitle">
            {isDepositMode ? 'Deposited Successfully into ' : 'Paid Successfully to '}
            <b>{receiverDisplayName}</b>
          </p>
        </div>

        {/* Official Banking Receipt Card */}
        <div className="banking-receipt-card">
          <div className="receipt-row">
            <span className="receipt-label">{isDepositMode ? 'Deposit Source' : 'Transfer Mode'}</span>
            <span className="receipt-value highlight-mode">
              {mode || (isDepositMode ? 'Razorpay Self-Deposit' : 'Instant UPI Transfer')}
            </span>
          </div>

          <div className="receipt-row">
            <span className="receipt-label">Transaction ID</span>
            <span className="receipt-value mono">{txnData.transactionId}</span>
          </div>

          <div className="receipt-row">
            <span className="receipt-label">Bank Reference No (UTR)</span>
            <span className="receipt-value mono">6239{Math.floor(10000000 + Math.random() * 90000000)}</span>
          </div>

          <div className="receipt-row">
            <span className="receipt-label">{isDepositMode ? 'Credited Account' : 'Debited Account'}</span>
            <span className="receipt-value">YourBank Savings A/C</span>
          </div>

          <div className="receipt-row">
            <span className="receipt-label">Updated Balance</span>
            <span className="receipt-value bold">
              ₹{parseFloat(txnData.balanceAfter).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </span>
          </div>

          <div className="receipt-row">
            <span className="receipt-label">Date & Time</span>
            <span className="receipt-value">{formattedDate}</span>
          </div>

          {txnData.remarks && (
            <div className="receipt-row">
              <span className="receipt-label">Remarks</span>
              <span className="receipt-value">{txnData.remarks}</span>
            </div>
          )}
        </div>

        <div className="security-guarantee-footer">
          256-Bit SSL Encrypted & Verified by YourBank Security Engine
        </div>

        <div className="modal-action-row" style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
          <button className="secondary-button full-width" onClick={() => window.print()}>
            Print Receipt
          </button>
          <button className="primary-button full-width" onClick={onClose}>
            Done
          </button>
        </div>
      </div>
    </div>
  )
}
