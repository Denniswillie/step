// Copyright 2019 Google LLC

const helloElement = document.querySelector(".typing");
//add "typing" feature for h1 tag "Hello" on index.html page

var textIndex = 0;
var text = "Hello";
var timeOutTimeMilliSeconds = 50;
var typer = true;
var transition = true;

function startTypingAnimation(){
    if(textIndex < text.length){
        if(typer){
            helloElement.innerHTML = "|";
            typer = false;
            setTimeout(startTypingAnimation, 500);
        }
        else{
            if(transition){
                helloElement.innerHTML = text.charAt(textIndex);
                transition = false;
                textIndex++;
            }
            else{
                helloElement.innerHTML += text.charAt(textIndex);
                textIndex++;
            }
            setTimeout(startTypingAnimation, timeOutTimeMilliSeconds);
        }
    }
    else{
        if(!typer){
            helloElement.innerHTML += "|";
            typer = true;
        }
        else{
            helloElement.innerHTML = helloElement.innerText.slice(0,helloElement.innerText.length - 1);
            typer = false;
        }
        setTimeout(startTypingAnimation, 400);
    }
};