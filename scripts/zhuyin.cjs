const initials = {b:'ㄅ',p:'ㄆ',m:'ㄇ',f:'ㄈ',d:'ㄉ',t:'ㄊ',n:'ㄋ',l:'ㄌ',g:'ㄍ',k:'ㄎ',h:'ㄏ',j:'ㄐ',q:'ㄑ',x:'ㄒ',zh:'ㄓ',ch:'ㄔ',sh:'ㄕ',r:'ㄖ',z:'ㄗ',c:'ㄘ',s:'ㄙ'};
const finals = {a:'ㄚ',o:'ㄛ',e:'ㄜ',eh:'ㄝ',ai:'ㄞ',ei:'ㄟ',ao:'ㄠ',ou:'ㄡ',an:'ㄢ',en:'ㄣ',ang:'ㄤ',eng:'ㄥ',er:'ㄦ',i:'ㄧ',ia:'ㄧㄚ',io:'ㄧㄛ',ie:'ㄧㄝ',iao:'ㄧㄠ',iou:'ㄧㄡ',ian:'ㄧㄢ',in:'ㄧㄣ',iang:'ㄧㄤ',ing:'ㄧㄥ',u:'ㄨ',ua:'ㄨㄚ',uo:'ㄨㄛ',uai:'ㄨㄞ',uei:'ㄨㄟ',uan:'ㄨㄢ',uen:'ㄨㄣ',uang:'ㄨㄤ',ueng:'ㄨㄥ',ong:'ㄨㄥ',v:'ㄩ',ve:'ㄩㄝ',van:'ㄩㄢ',vn:'ㄩㄣ',iong:'ㄩㄥ'};
function zhuyin(value) {
  let s=value.toLowerCase().normalize('NFD'), tone=5;
  const marks={'\u0304':1,'\u0301':2,'\u030c':3,'\u0300':4};
  for(const mark of s) if(marks[mark]) tone=marks[mark];
  const numeric=s.match(/[0-5]$/); if(numeric) tone=Number(numeric[0])||5;
  s=s.replace(/u\u0308/g,'v').replace(/e\u0302/g,'eh').replace(/[\u0300-\u036f0-5]/g,'');
  if(s.startsWith('y')) { const y={yi:'i',ya:'ia',yo:'io',ye:'ie',yao:'iao',you:'iou',yan:'ian',yin:'in',yang:'iang',ying:'ing',yong:'iong',yu:'v',yue:'ve',yuan:'van',yun:'vn'}; s=y[s]||s; }
  if(s.startsWith('w')) { const w={wu:'u',wa:'ua',wo:'uo',wai:'uai',wei:'uei',wan:'uan',wen:'uen',wang:'uang',weng:'ueng'}; s=w[s]||s; }
  let initial='',rest=s;
  for(const key of Object.keys(initials).sort((a,b)=>b.length-a.length)) if(s.startsWith(key)){initial=key;rest=s.slice(key.length);break;}
  if(['j','q','x'].includes(initial)&&rest.startsWith('u'))rest='v'+rest.slice(1);
  if(rest==='iu')rest='iou';if(rest==='ui')rest='uei';if(rest==='un')rest='uen';
  let body;
  if(['zh','ch','sh','r','z','c','s'].includes(initial)&&rest==='i')body=initials[initial];
  else if(s==='m')body='ㄇ';else if(s==='n')body='ㄣ';else if(s==='ng')body='ㄥ';
  else if(finals[rest])body=(initials[initial]||'')+finals[rest];else return null;
  return tone===5?'˙'+body:body+({2:'ˊ',3:'ˇ',4:'ˋ'}[tone]||'');
}
module.exports={zhuyin};
