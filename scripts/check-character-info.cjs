const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const {zhuyin}=require('./zhuyin.cjs');
const cases={chén:'ㄔㄣˊ',yǒng:'ㄩㄥˇ',xué:'ㄒㄩㄝˊ',yue4:'ㄩㄝˋ',jūn:'ㄐㄩㄣ',yuán:'ㄩㄢˊ',lǜ:'ㄌㄩˋ',nǚ:'ㄋㄩˇ',lüè:'ㄌㄩㄝˋ',zhī:'ㄓ',zī:'ㄗ',rì:'ㄖˋ',de:'˙ㄉㄜ',liu2:'ㄌㄧㄡˊ',gui3:'ㄍㄨㄟˇ',dun4:'ㄉㄨㄣˋ',xiong2:'ㄒㄩㄥˊ',you3:'ㄧㄡˇ',weng1:'ㄨㄥ',shui3:'ㄕㄨㄟˇ',zhi1:'ㄓ',hao3:'ㄏㄠˇ'};
for(const [pinyin,result]of Object.entries(cases))assert.equal(zhuyin(pinyin),result,pinyin);
assert.equal(zhuyin('not-a-syllable'),null);
const root=path.join(__dirname,'../app/src/main/assets');
const dict=JSON.parse(fs.readFileSync(path.join(root,'character-info.json'),'utf8'));
assert.deepEqual(dict['陳'],{readings:['ㄔㄣˊ'],radical:'阜（阝）'});
assert.deepEqual(dict['重'].readings,['ㄓㄨㄥˋ','ㄔㄨㄥˊ']);
assert.ok(dict['行'].readings.includes('ㄏㄤˊ'));
assert.deepEqual(dict['髮'],{readings:['ㄈㄚˋ'],radical:'髟'});
for(const filename of fs.readdirSync(path.join(root,'data'))){const char=path.basename(filename,'.json');assert.ok(dict[char]?.readings.length,`Missing reading for ${char}`);assert.ok(dict[char].radical,`Missing radical for ${char}`);}
for(const entry of Object.values(dict))for(const reading of entry.readings)assert.match(reading,/^[\u3105-\u312fˊˇˋ˙]+$/);
console.log(`${Object.keys(cases).length} phonetic conversion cases passed; checked ${Object.keys(dict).length} metadata records and bundled glyph coverage.`);
