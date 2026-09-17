import { ai } from 'hatchable';

export const access = 'user';
export const methods = ['POST'];

export default async function (req, res) {
  const prompt = String(req.body?.prompt || '').trim().slice(0, 4000);
  if (!prompt) return res.status(400).json({ error: 'prompt is required' });
  const result = await ai.generateText({ model: 'sonnet', purpose: 'safety-analysis', system: 'You are a digital safety analyst. Be evidence-constrained. Explicitly distinguish VERIFIED FACT, INDICATOR, POSSIBILITY, and UNKNOWN. Never claim a device is spyware-free from absence of indicators. Never infer a person is trustworthy, untrustworthy, good, bad, or dangerous from incomplete evidence. Never invent sources, records, dates, verification results, device telemetry, or evidence. Do not provide covert surveillance instructions.', prompt, maxSteps: 2 });
  return res.json({ analysis: result.text || result });
}