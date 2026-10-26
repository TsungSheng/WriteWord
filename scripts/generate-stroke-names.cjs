// Run after extracting cnchar, cnchar-order and cnchar-trad 3.2.6 into .tools/cnchar.
// Dictionary only; no third-party JavaScript is shipped in the Android app.
const fs = require('fs');
const path = require('path');
const root = path.resolve(__dirname, '..');
const cnchar = require(path.join(root, '.tools/cnchar/cnchar/package'));
cnchar.use(require(path.join(root, '.tools/cnchar/cnchar-order/package')),
  require(path.join(root, '.tools/cnchar/cnchar-trad/package')));
const dict = {};
const traditional = {横:'橫',竖:'豎',钩:'鉤',点:'點',弯:'彎',卧:'臥'};
for (let cp = 0x3400; cp <= 0x9fff; cp++) {
  const char = String.fromCodePoint(cp);
  const names = cnchar.stroke(char, 'order', 'name')[0];
  if (!Array.isArray(names) || !names.length) continue;
  dict[char] = names.map(name => name.replace(/2/g, '').replace(/[横竖钩点弯卧]/g, c => traditional[c]));
}
// Overrides were checked against the bundled Make Me a Hanzi medians, whose
// sequence can differ from cnchar. Keep these isolated and reviewable.
Object.assign(dict, JSON.parse(fs.readFileSync(path.join(root, 'app/src/main/assets/stroke-name-overrides.json'), 'utf8')));
fs.writeFileSync(path.join(root, 'app/src/main/assets/stroke-names.json'), JSON.stringify(dict));
fs.copyFileSync(path.join(root, '.tools/cnchar/cnchar-order/package/LICENSE'),
  path.join(root, 'app/src/main/assets/licenses/CNCHAR-MIT.txt'));
console.log(`Generated stroke names for ${Object.keys(dict).length} characters.`);
