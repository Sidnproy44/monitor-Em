import { db } from 'hatchable';
import { handleAuditRead } from 'lib/audit-reader.js';

export const access = 'user';
export const methods = ['GET'];

export default async function (req, res) {
  return handleAuditRead(req, res, db);
}
