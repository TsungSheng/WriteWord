const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.join(__dirname, '../app/src/main/assets');
const names = JSON.parse(fs.readFileSync(path.join(root, 'stroke-names.json'), 'utf8'));
assert.deepEqual(names['永'], ['點', '橫折鉤', '橫撇|橫鉤', '撇', '捺']);
for (const file of fs.readdirSync(path.join(root, 'data'))) {
  const char = path.basename(file, '.json');
  const data = JSON.parse(fs.readFileSync(path.join(root, 'data', file), 'utf8'));
  assert.equal(data.strokes.length, data.medians.length);
  assert.equal(names[char]?.length, data.strokes.length, `Names and glyph strokes disagree for ${char}`);
}
for (const [char, strokes] of Object.entries(names)) {
  assert.ok(strokes.length && strokes.every(name => typeof name === 'string' && name.length));
  assert.ok(strokes.every(name => !/[横竖钩点弯卧2]/.test(name)), `Unconverted label: ${char}`);
}
console.log(`All ${fs.readdirSync(path.join(root, 'data')).length} bundled glyphs have matching name counts; validated ${Object.keys(names).length} dictionary entries.`);
