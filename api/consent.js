import { db } from 'hatchable';

export const access = 'user';
export const methods = ['GET','POST'];

const ALLOWED = new Set(['Location','Screen time','App usage','Call metadata','Contacts','Gallery','Selected files','Additional device information']);

export default async function (req, res) {
  if (req.method === 'POST') {
    const capability = String(req.body?.capability || '').trim();
    const granted = req.body?.granted;
    const purpose = String(req.body?.purpose || '').trim().slice(0, 500) || null;
    const scope = req.body?.scope && typeof req.body.scope === 'object' && !Array.isArray(req.body.scope) ? req.body.scope : {};
    const expiresAt = req.body?.expires_at ? new Date(req.body.expires_at) : null;
    if (!ALLOWED.has(capability) || typeof granted !== 'boolean') return res.status(400).json({ error: 'Invalid capability or granted value' });
    if (expiresAt && Number.isNaN(expiresAt.getTime())) return res.status(400).json({ error: 'Invalid expires_at value' });

    const { rows: saved } = await db.query(
      'INSERT INTO consent_records (user_id, subject_user_id, requesting_user_id, capability, purpose, scope, granted, granted_at, revoked_at, status, expires_at, consent_version) VALUES ($1, $1, $1, $2, $3, $4, $5, CASE WHEN $5 THEN now() ELSE NULL END, CASE WHEN $5 THEN NULL ELSE now() END, CASE WHEN $5 THEN \'active\' ELSE \'revoked\' END, $6, \'1\') ON CONFLICT (subject_user_id, requesting_user_id, capability) DO UPDATE SET subject_user_id = EXCLUDED.subject_user_id, requesting_user_id = EXCLUDED.requesting_user_id, purpose = EXCLUDED.purpose, scope = EXCLUDED.scope, granted = EXCLUDED.granted, granted_at = EXCLUDED.granted_at, revoked_at = EXCLUDED.revoked_at, status = EXCLUDED.status, expires_at = EXCLUDED.expires_at, consent_version = EXCLUDED.consent_version, updated_at = now() RETURNING id, capability, status, granted, expires_at, consent_version',
      [req.user.id, capability, purpose, JSON.stringify(scope), granted, expiresAt ? expiresAt.toISOString() : null]
    );
    const record = saved[0];
    await db.query(
      'INSERT INTO access_audit_log (owner_user_id, actor_user_id, resource_type, resource_id, action, purpose, authorization_context, consent_id, outcome) VALUES ($1, $1, \'consent\', $2, $3, $4, $5, $6, \'allowed\')',
      [req.user.id, record.id, granted ? 'consent.created_or_granted' : 'consent.revoked', purpose, JSON.stringify({ capability, scope, consent_version: '1' }), record.id]
    );
  }
  const { rows } = await db.query('SELECT capability, subject_user_id, requesting_user_id, purpose, scope, granted, status, granted_at, revoked_at, expires_at, consent_version, updated_at FROM consent_records ORDER BY capability');
  return res.json({ consents: rows });
}