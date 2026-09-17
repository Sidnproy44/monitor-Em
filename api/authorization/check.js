import { db } from 'hatchable';
import { handleAuthorizationCheck } from 'lib/authorization-api.js';

export const access = 'user';
export const methods = ['POST'];

export default async function (req, res) {
  return handleAuthorizationCheck(req, res, db);
}