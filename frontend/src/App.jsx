
import React, { useEffect, useState } from 'react';
import { api } from './api';

function Auth({ done }) {
  const [login, setLogin] = useState(true);
  const [f, setF] = useState({ name: '', email: '', password: '' });
  const [err, setErr] = useState('');

  const go = async (e) => {
    e.preventDefault();
    setErr('');

    try {
      const { data } = await api.post(
        login ? '/auth/login' : '/auth/register',
        f
      );

      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data));
      done(data);
    } catch (x) {
      setErr(x.response?.data?.error || 'Request failed');
    }
  };

  return (
    <main className="auth">
      <form onSubmit={go}>
        <h1>
          Lexi<span>Scan</span> AI
        </h1>

        {!login && (
          <input
            placeholder="Name"
            required
            value={f.name}
            onChange={(e) => setF({ ...f, name: e.target.value })}
          />
        )}

        <input
          type="email"
          placeholder="Email"
          required
          value={f.email}
          onChange={(e) => setF({ ...f, email: e.target.value })}
        />

        <input
          type="password"
          placeholder="Password"
          required
          value={f.password}
          onChange={(e) => setF({ ...f, password: e.target.value })}
        />

        {err && <p className="err">{err}</p>}

        <button type="submit">
          {login ? 'Login' : 'Register'}
        </button>

        <a
          onClick={() => {
            setLogin(!login);
            setErr('');
          }}
        >
          {login ? 'Create account' : 'Back to login'}
        </a>
      </form>
    </main>
  );
}

function App() {
  const [user, setUser] = useState(() =>
    JSON.parse(localStorage.getItem('user') || 'null')
  );

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return user ? (
    <Dash user={user} logout={logout} />
  ) : (
    <Auth done={setUser} />
  );
}

function Dash({ user, logout }) {
  const [docs, setDocs] = useState([]);
  const [sel, setSel] = useState(null);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');

  const load = async () => {
    try {
      const { data } = await api.get('/documents');
      setDocs(data);
    } catch (e) {
      setErr(e.response?.data?.error || 'Could not load documents');
    }
  };

  useEffect(() => {
    load();
  }, []);

  const upload = async (e) => {
    const file = e.target.files?.[0];

    if (!file) return;

    if (!file.name.toLowerCase().endsWith('.pdf')) {
      setErr('Please select a PDF file.');
      e.target.value = '';
      return;
    }

    setBusy(true);
    setErr('');

    const fd = new FormData();
    fd.append('file', file);

    try {
      const { data } = await api.post('/documents/upload', fd);

      setDocs((current) => [
        data,
        ...current.filter((d) => d.id !== data.id),
      ]);

      setSel(data);
    } catch (x) {
      setErr(x.response?.data?.error || 'Document analysis failed');
      await load();
    } finally {
      setBusy(false);
      e.target.value = '';
    }
  };

  const a = sel?.analysis || {};

  const statusLabel = (status) => {
    const value = String(status || 'UNKNOWN').toUpperCase();

    if (value === 'ANALYZED') return 'Analyzed';
    if (value === 'ANALYZING') return 'Analyzing';
    if (value === 'EXTRACTING') return 'Extracting';
    if (value === 'FAILED') return 'Failed';

    return value;
  };

  return (
    <>
      <header>
        <b>
          Lexi<span>Scan</span> AI
        </b>

        <span className="header-user">
          {user.name || user.email || 'User'}
          <button onClick={logout}>Logout</button>
        </span>
      </header>

      <main>
        <section className="top">
          <div>
            <h2>Document Intelligence</h2>
            <p>
              Upload a contract and get an AI-powered analysis.
            </p>
          </div>

          <label
            className={`upload ${busy ? 'upload-disabled' : ''}`}
            aria-disabled={busy}
          >
            {busy ? 'Analyzing document…' : '+ Upload PDF'}
            <input
              type="file"
              accept=".pdf,application/pdf"
              disabled={busy}
              onChange={upload}
            />
          </label>
        </section>

        {err && <p className="err">{err}</p>}

        <div className="layout">
          <aside>
            
            <div className="documents-heading">
            <h3>Documents</h3>
            <div className="document-actions">
                <span className="document-count">{docs.length}</span>
                <button
                type="button"
                className="refresh-button"
                onClick={load}
                title="Refresh documents"
                aria-label="Refresh documents"
                >
                ↻
                </button>
            </div>
            </div>


            {docs.length === 0 ? (
              <p className="muted">No documents uploaded yet.</p>
            ) : (
              docs.map((d) => (
                <div
                  key={d.id}
                  className={`doc ${sel?.id === d.id ? 'active' : ''}`}
                  onClick={() => setSel(d)}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault();
                      setSel(d);
                    }
                  }}
                >
                  <b>{d.fileName}</b>

                  <small>
                    {d.createdAt
                      ? new Date(d.createdAt).toLocaleString()
                      : 'Date unavailable'}
                  </small>

                  <span
                    className={`status-badge ${String(
                      d.status || 'unknown'
                    ).toLowerCase()}`}
                  >
                    <span className="status-dot" />
                    {statusLabel(d.status)}
                  </span>
                </div>
              ))
            )}
          </aside>

          <article>
            {!sel ? (
              <div className="empty">
                <h2>Upload a document</h2>
                <p>Your AI analysis will appear here.</p>
              </div>
            ) : (
              <>
                <div className="title">
                  <div>
                    <p className="eyebrow">DOCUMENT ANALYSIS</p>
                    <h2>{sel.fileName}</h2>
                  </div>

                  {a.riskLevel && (
                    <strong
                      className={`risk ${String(
                        a.riskLevel
                      ).toLowerCase()}`}
                    >
                      {a.riskLevel} RISK
                    </strong>
                  )}
                </div>

                {sel.status !== 'ANALYZED' ? (
                  <section className="card">
                    <h3>Analysis status</h3>
                    <span
                      className={`status-badge ${String(
                        sel.status || 'unknown'
                      ).toLowerCase()}`}
                    >
                      {statusLabel(sel.status)}
                    </span>

                    <p className="status-message">
                      {sel.status === 'FAILED'
                        ? 'This document could not be analyzed. Please try uploading it again.'
                        : 'Document processing has not completed yet.'}
                    </p>
                  </section>
                ) : (
                  <>
                    <section className="card">
                      <h3>Summary</h3>
                      <p>{a.summary || 'No summary available.'}</p>
                    </section>

                    <div className="two">
                      <section className="card">
                        <h3>Risks</h3>

                        {(a.risks || []).length ? (
                          <ul>
                            {a.risks.map((risk, i) => (
                              <li key={i}>{risk}</li>
                            ))}
                          </ul>
                        ) : (
                          <p>No risks were identified by the analysis.</p>
                        )}
                      </section>

                      <section className="card">
                        <h3>Key Information</h3>

                        {Object.keys(a.keyInformation || {}).length ? (
                          <ul>
                            {Object.entries(a.keyInformation).map(
                              ([key, value]) => (
                                <li key={key}>
                                  <b>
                                    {key
                                      .replace(/([A-Z])/g, ' $1')
                                      .replace(/^./, (s) => s.toUpperCase())}
                                  </b>
                                  : {String(value ?? 'Not found')}
                                </li>
                              )
                            )}
                          </ul>
                        ) : (
                          <p>No key information available.</p>
                        )}
                      </section>
                    </div>

                    <section className="card">
                      <h3>Important Clauses</h3>

                      {(a.importantClauses || []).length ? (
                        a.importantClauses.map((clause, i) => (
                          <div className="clause" key={i}>
                            <b>{clause.name || 'Unnamed clause'}</b>
                            <p>{clause.explanation || 'No explanation available.'}</p>
                            <small>Risk: {clause.risk || 'Not specified'}</small>
                          </div>
                        ))
                      ) : (
                        <p>No important clauses were identified.</p>
                      )}
                    </section>

                    <section className="card">
                      <h3>Obligations</h3>

                      {(a.obligations || []).length ? (
                        <ul>
                          {a.obligations.map((obligation, i) => (
                            <li key={i}>{obligation}</li>
                          ))}
                        </ul>
                      ) : (
                        <p>No obligations were identified.</p>
                      )}
                    </section>

                    <p className="disclaimer">
                      {a.disclaimer ||
                        'LexiScan AI provides informational document analysis and does not provide legal advice.'}
                    </p>
                  </>
                )}
              </>
            )}
          </article>
        </div>
      </main>
    </>
  );
}

export default App;
