const fs=require('node:fs'),path=require('node:path');
const {zhuyin}=require('./zhuyin.cjs');
const root=path.resolve(__dirname,'..');
const c=require(path.join(root,'.tools/cnchar/cnchar/package'));
c.use(require(path.join(root,'.tools/cnchar/cnchar-trad/package')),require(path.join(root,'.tools/cnchar/cnchar-radical/package')));
const overrides={'髮':{radical:'髟',pinyin:['fà']},'陳':{radical:'阜（阝）'},'黃':{radical:'黃'}};
const dict={};let missing=0;
for(let cp=0x3400;cp<=0x9fff;cp++){
 const char=String.fromCodePoint(cp);let pinyin=[],radical='';
 try{const value=c.spell(char,'poly','tone','low','array')[0];if(value&&value!==char)pinyin=value.replace(/[()]/g,'').split('|');}catch(e){missing++;}
 try{radical=c.radical(char)[0]?.radical||'';}catch(e){}
 if(overrides[char]?.pinyin)pinyin=overrides[char].pinyin;
 if(overrides[char]?.radical)radical=overrides[char].radical;
 const readings=[...new Set(pinyin.map(zhuyin).filter(Boolean))];
 if(readings.length||radical)dict[char]={readings,radical};
}
fs.writeFileSync(path.join(root,'app/src/main/assets/character-info.json'),JSON.stringify(dict));
// Native vector artwork uses the original stroke paths, not a generated bitmap.
const strokes=JSON.parse(fs.readFileSync(path.join(root,'app/src/main/assets/data/陳.json'),'utf8')).strokes;
const vector=(adaptive)=>`<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="108dp" android:height="108dp" android:viewportWidth="1024" android:viewportHeight="1024">\n${adaptive?'':'<path android:fillColor="#16765B" android:pathData="M0,0H1024V1024H0Z"/>'}\n<group android:pivotX="512" android:pivotY="512" android:scaleX="${adaptive?.60:.82}" android:scaleY="${adaptive?.60:.82}"><group android:scaleY="-1" android:translateY="900">\n${strokes.map(p=>`<path android:fillColor="#FFFFFF" android:pathData="${p}"/>`).join('\n')}\n</group></group>\n</vector>\n`;
const res=path.join(root,'app/src/main/res');fs.mkdirSync(path.join(res,'drawable'),{recursive:true});
fs.writeFileSync(path.join(res,'drawable/ic_launcher_foreground.xml'),vector(true));
fs.writeFileSync(path.join(res,'drawable/ic_launcher_legacy.xml'),vector(false));
console.log(`Generated ${Object.keys(dict).length} character records and 陳 launcher vectors. Unavailable spellings: ${missing}.`);
