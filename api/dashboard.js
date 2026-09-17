import { db } from 'hatchable';

export const access = 'user';
export const methods = ['GET'];

export default async function (req, res) {
  const { rows: profiles } = await db.query('SELECT display_name, created_at FROM safety_profiles ORDER BY created_at DESC LIMIT 1');
  const { rows: events } = await db.query('SELECT category, severity, title, detail, status, created_at FROM safety_events ORDER BY created_at DESC LIMIT 20');
  const { rows: consents } = await db.query('SELECT capability, granted, granted_at, revoked_at, updated_at FROM consent_records ORDER BY capability');
  return res.json({ signedIn: true, profile: profiles[0] || null, events, consents });
}