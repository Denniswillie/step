async function fetchFromData(){
    const res = await fetch("/data");
    const text = await res.text();
    document.getElementById("fetchFromData").innerText += text;
}

document.body.onload = fetchFromData();