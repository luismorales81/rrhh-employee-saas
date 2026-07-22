const fs = require('fs');
const path = require('path');

async function principal() {
  const sharp = require('sharp');
  const toIco = require('to-ico');
  const svg = fs.readFileSync(path.join('public', 'favicon.svg'));
  const buffers = await Promise.all(
    [16, 32, 48].map((tamano) => sharp(svg).resize(tamano, tamano).png().toBuffer())
  );
  const ico = await toIco(buffers);
  fs.writeFileSync(path.join('public', 'favicon.ico'), ico);
  console.log('favicon.ico generado:', ico.length, 'bytes');
}

principal().catch((error) => {
  console.error(error);
  process.exit(1);
});
