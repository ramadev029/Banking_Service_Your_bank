import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

export default function QaTriageDashboard() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [summary, setSummary] = useState(null)
  const [evalResult, setEvalResult] = useState(null)
  const [selectedDraft, setSelectedDraft] = useState(null)
  const [approvalMessage, setApprovalMessage] = useState('')

  const fetchDashboardData = async () => {
    setLoading(true)
    try {
      const res = await fetch('http://localhost:8080/api/v1/triage/dashboard-summary')
      if (res.ok) {
        const data = await res.json()
        setSummary(data)
      }
    } catch (e) {
      console.error('Failed to fetch triage summary:', e)
    } finally {
      setLoading(false)
    }
  }

  const runEvaluationHarness = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/v1/triage/evaluation-matrix')
      if (res.ok) {
        const data = await res.json()
        setEvalResult(data)
      }
    } catch (e) {
      console.error('Failed to run evaluation harness:', e)
    }
  }

  const handleApproveJiraDraft = async (draftId) => {
    try {
      const res = await fetch(`http://localhost:8080/api/v1/triage/approve-jira/${draftId}`, {
        method: 'POST'
      })
      if (res.ok) {
        setApprovalMessage('✅ Defect Draft approved by QA Lead & submitted to Jira successfully!')
        setSelectedDraft(null)
        fetchDashboardData()
      }
    } catch (e) {
      console.error('Jira approval failed:', e)
    }
  }

  useEffect(() => {
    fetchDashboardData()
  }, [])

  if (loading) {
    return (
      <div style={{ padding: '60px', textAlign: 'center', fontFamily: 'sans-serif' }}>
        <h2>🤖 Loading AI Triage Assistant Metrics...</h2>
      </div>
    )
  }

  const categoryCounts = summary?.categoryCounts || {}
  const recentClassifications = summary?.recentClassifications || []
  const pendingApprovalDrafts = summary?.pendingApprovalDrafts || []
  const quarantinedTests = summary?.quarantinedTests || []

  return (
    <div style={{ background: '#0f172a', color: '#f8fafc', minHeight: '100vh', padding: '30px', fontFamily: 'Segoe UI, Tahoma, sans-serif' }}>
      
      {/* Top Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px', borderBottom: '1px solid #334155', pb: '20px' }}>
        <div>
          <h1 style={{ margin: 0, fontSize: '28px', color: '#38bdf8' }}>🤖 AI-Powered Test Execution Failure Triage Assistant</h1>
          <p style={{ margin: '5px 0 0 0', color: '#94a3b8' }}>Human-in-the-Loop QA Automation Tooling | 4-Way Classifier & Flakiness Tracker</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button onClick={fetchDashboardData} style={{ background: '#1e293b', border: '1px solid #475569', color: '#e2e8f0', padding: '10px 18px', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>🔄 Refresh</button>
          <button onClick={runEvaluationHarness} style={{ background: '#0284c7', border: 'none', color: '#fff', padding: '10px 18px', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>📊 Run Module 7 Evaluation Matrix</button>
          <button onClick={() => navigate('/')} style={{ background: '#334155', border: 'none', color: '#fff', padding: '10px 18px', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>🏦 Exit to Banking App</button>
        </div>
      </div>

      {approvalMessage && (
        <div style={{ background: '#064e3b', border: '1px solid #10b981', color: '#a7f3d0', padding: '14px', borderRadius: '8px', marginBottom: '25px', fontWeight: 600 }}>
          {approvalMessage}
        </div>
      )}

      {/* Top Metrics Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '30px' }}>
        <div style={{ background: '#1e293b', padding: '20px', borderRadius: '12px', borderLeft: '5px solid #38bdf8' }}>
          <span style={{ fontSize: '13px', color: '#94a3b8', textTransform: 'uppercase' }}>Suite Health Index</span>
          <h2 style={{ fontSize: '32px', margin: '10px 0 0 0', color: '#38bdf8' }}>{summary?.suiteHealthScore?.toFixed(1)}%</h2>
        </div>
        <div style={{ background: '#1e293b', padding: '20px', borderRadius: '12px', borderLeft: '5px solid #f43f5e' }}>
          <span style={{ fontSize: '13px', color: '#94a3b8', textTransform: 'uppercase' }}>Defects Awaiting QA Approval</span>
          <h2 style={{ fontSize: '32px', margin: '10px 0 0 0', color: '#f43f5e' }}>{pendingApprovalDrafts.length}</h2>
        </div>
        <div style={{ background: '#1e293b', padding: '20px', borderRadius: '12px', borderLeft: '5px solid #f59e0b' }}>
          <span style={{ fontSize: '13px', color: '#94a3b8', textTransform: 'uppercase' }}>Quarantined Flaky Tests (≥25%)</span>
          <h2 style={{ fontSize: '32px', margin: '10px 0 0 0', color: '#f59e0b' }}>{quarantinedTests.length}</h2>
        </div>
        <div style={{ background: '#1e293b', padding: '20px', borderRadius: '12px', borderLeft: '5px solid #10b981' }}>
          <span style={{ fontSize: '13px', color: '#94a3b8', textTransform: 'uppercase' }}>Total Failures Analyzed</span>
          <h2 style={{ fontSize: '32px', margin: '10px 0 0 0', color: '#10b981' }}>{recentClassifications.length}</h2>
        </div>
      </div>

      {/* 4-Way Failure Category Breakdown */}
      <div style={{ background: '#1e293b', padding: '24px', borderRadius: '12px', marginBottom: '30px' }}>
        <h3 style={{ margin: '0 0 16px 0', color: '#e2e8f0' }}>📌 Module 3: 4-Way Failure Category Breakdown</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '15px' }}>
          <div style={{ background: '#0f172a', padding: '16px', borderRadius: '8px', border: '1px solid #ef4444' }}>
            <span style={{ color: '#fca5a5', fontWeight: 600 }}>🔴 Genuine Functional Defect</span>
            <h3 style={{ margin: '8px 0 0 0', fontSize: '24px', color: '#fff' }}>{categoryCounts.GENUINE_FUNCTIONAL_DEFECT || 0}</h3>
          </div>
          <div style={{ background: '#0f172a', padding: '16px', borderRadius: '8px', border: '1px solid #f59e0b' }}>
            <span style={{ color: '#fde68a', fontWeight: 600 }}>⚠️ Flaky / Unstable Test</span>
            <h3 style={{ margin: '8px 0 0 0', fontSize: '24px', color: '#fff' }}>{categoryCounts.FLAKY_UNSTABLE_TEST || 0}</h3>
          </div>
          <div style={{ background: '#0f172a', padding: '16px', borderRadius: '8px', border: '1px solid #3b82f6' }}>
            <span style={{ color: '#93c5fd', fontWeight: 600 }}>🌐 Environment / Data Issue</span>
            <h3 style={{ margin: '8px 0 0 0', fontSize: '24px', color: '#fff' }}>{categoryCounts.ENVIRONMENT_DATA_ISSUE || 0}</h3>
          </div>
          <div style={{ background: '#0f172a', padding: '16px', borderRadius: '8px', border: '1px solid #8b5cf6' }}>
            <span style={{ color: '#ddd6fe', fontWeight: 600 }}>🛠️ Test Script Issue</span>
            <h3 style={{ margin: '8px 0 0 0', fontSize: '24px', color: '#fff' }}>{categoryCounts.TEST_SCRIPT_ISSUE || 0}</h3>
          </div>
        </div>
      </div>

      {/* Jira Draft Approval Panel (Human-in-the-Loop) */}
      <div style={{ background: '#1e293b', padding: '24px', borderRadius: '12px', marginBottom: '30px' }}>
        <h3 style={{ margin: '0 0 16px 0', color: '#f43f5e' }}>🛡️ Module 5: Human-in-the-Loop Jira Defect Approval Queue</h3>
        {pendingApprovalDrafts.length === 0 ? (
          <p style={{ color: '#94a3b8' }}>No pending defect drafts. All genuine defects have been reviewed by QA.</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {pendingApprovalDrafts.map(draft => (
              <div key={draft.id} style={{ background: '#0f172a', padding: '16px', borderRadius: '8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <h4 style={{ margin: 0, color: '#fca5a5' }}>{draft.testName}</h4>
                  <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: '#94a3b8' }}>{draft.writtenReasoning}</p>
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                  <button onClick={() => setSelectedDraft(draft)} style={{ background: '#334155', border: 'none', color: '#fff', padding: '8px 14px', borderRadius: '6px', cursor: 'pointer' }}>Preview Draft</button>
                  <button onClick={() => handleApproveJiraDraft(draft.id)} style={{ background: '#10b981', border: 'none', color: '#fff', padding: '8px 14px', borderRadius: '6px', cursor: 'pointer', fontWeight: 600 }}>Approve & Submit to Jira</button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Module 7 Evaluation Matrix (If Executed) */}
      {evalResult && (
        <div style={{ background: '#1e293b', padding: '24px', borderRadius: '12px', marginBottom: '30px', border: '1px solid #0284c7' }}>
          <h3 style={{ margin: '0 0 16px 0', color: '#38bdf8' }}>📊 Module 7 Evaluation Harness Matrix & Accuracy Metrics</h3>
          <p style={{ color: '#94a3b8' }}>Total Hand-Labeled Benchmark Cases: <strong>{evalResult.totalCases}</strong> | Correct Predictions: <strong>{evalResult.correctPredictions}</strong> | Overall Accuracy: <strong>{evalResult.accuracyPercentage.toFixed(1)}%</strong></p>
          <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '15px' }}>
            <thead>
              <tr style={{ background: '#0f172a', color: '#94a3b8', textAlign: 'left' }}>
                <th style={{ padding: '10px' }}>Failure Category</th>
                <th style={{ padding: '10px' }}>Precision</th>
                <th style={{ padding: '10px' }}>Recall</th>
                <th style={{ padding: '10px' }}>F1-Score</th>
              </tr>
            </thead>
            <tbody>
              {Object.keys(evalResult.precisionPerCategory || {}).map(cat => (
                <tr key={cat} style={{ borderBottom: '1px solid #334155' }}>
                  <td style={{ padding: '10px', fontWeight: 600, color: '#e2e8f0' }}>{cat}</td>
                  <td style={{ padding: '10px', color: '#10b981' }}>{evalResult.precisionPerCategory[cat].toFixed(1)}%</td>
                  <td style={{ padding: '10px', color: '#38bdf8' }}>{evalResult.recallPerCategory[cat].toFixed(1)}%</td>
                  <td style={{ padding: '10px', color: '#f59e0b', fontWeight: 700 }}>{evalResult.f1PerCategory[cat].toFixed(1)}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Preview Modal */}
      {selectedDraft && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.8)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}>
          <div style={{ background: '#1e293b', width: '600px', padding: '24px', borderRadius: '12px', color: '#fff' }}>
            <h3 style={{ margin: 0, color: '#f43f5e' }}>Preview Jira Defect Draft</h3>
            <p style={{ color: '#94a3b8', fontSize: '14px' }}>Test: {selectedDraft.testName}</p>
            <div style={{ background: '#0f172a', padding: '12px', borderRadius: '6px', margin: '15px 0', fontSize: '13px', maxHeight: '200px', overflowY: 'auto' }}>
              <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{selectedDraft.jiraDraftPayload}</pre>
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button onClick={() => setSelectedDraft(null)} style={{ background: '#334155', border: 'none', color: '#fff', padding: '8px 16px', borderRadius: '6px', cursor: 'pointer' }}>Close</button>
              <button onClick={() => handleApproveJiraDraft(selectedDraft.id)} style={{ background: '#10b981', border: 'none', color: '#fff', padding: '8px 16px', borderRadius: '6px', cursor: 'pointer', fontWeight: 600 }}>Approve & Send</button>
            </div>
          </div>
        </div>
      )}

    </div>
  )
}
