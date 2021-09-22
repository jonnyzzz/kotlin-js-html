(function () {
  function isKotlinScriptNode(node) {
    if (node === undefined) {
      return false;
    }
    return node.tagName === 'SCRIPT' && node.type === 'language/kotlin';
  }

  function getScriptWithNode(node) {
    if (node === undefined || !isKotlinScriptNode(node)) {
      return undefined;
    }
    return {
      script: node.textContent,
      node: node
    };
  }

  function getKotlinScriptWithNode(mutation) {
    if (mutation === undefined) {
      return undefined;
    }

    if (mutation.type === 'childList') {
      for (const node of mutation.addedNodes) {
        const scriptWithNode = getScriptWithNode(node);
        if (scriptWithNode !== undefined) {
          return scriptWithNode;
        }
      }
    }
    if (mutation.type === 'characterData' || mutation.type === 'characterData') {
      return getScriptWithNode(mutation.target.parentNode);
    }

    return undefined;
  }

  function createRequestJSON(script) {
    if (script === undefined || 'string' !== typeof (script)) {
      return undefined;
    }

    return {"script": script};
  }

  function handleDocumentMutation(mutations) {
    for (const mutation of mutations) {
      const scriptWithNode = getKotlinScriptWithNode(mutation);
      if (scriptWithNode === undefined) {
        continue;
      }
      const script = scriptWithNode.script;
      const node = scriptWithNode.node;
      console.log(createRequestJSON(script));
      const animationNode = addAnimationNode(node);
    }
  }

  function addAnimationStyle() {
    const style = document.createElement('style');
    style.innerHTML = `
        .lds-spinner {
          color: official;
          display: inline-block;
          position: relative;
          width: 80px;
          height: 80px;
        }
        .lds-spinner div {
          transform-origin: 40px 40px;
          animation: lds-spinner 1.2s linear infinite;
        }
        .lds-spinner div:after {
          content: " ";
          display: block;
          position: absolute;
          top: 3px;
          left: 37px;
          width: 6px;
          height: 18px;
          border-radius: 20%;
          background: #787878;
        }
        .lds-spinner div:nth-child(1) {
          transform: rotate(0deg);
          animation-delay: -1.1s;
        }
        .lds-spinner div:nth-child(2) {
          transform: rotate(30deg);
          animation-delay: -1s;
        }
        .lds-spinner div:nth-child(3) {
          transform: rotate(60deg);
          animation-delay: -0.9s;
        }
        .lds-spinner div:nth-child(4) {
          transform: rotate(90deg);
          animation-delay: -0.8s;
        }
        .lds-spinner div:nth-child(5) {
          transform: rotate(120deg);
          animation-delay: -0.7s;
        }
        .lds-spinner div:nth-child(6) {
          transform: rotate(150deg);
          animation-delay: -0.6s;
        }
        .lds-spinner div:nth-child(7) {
          transform: rotate(180deg);
          animation-delay: -0.5s;
        }
        .lds-spinner div:nth-child(8) {
          transform: rotate(210deg);
          animation-delay: -0.4s;
        }
        .lds-spinner div:nth-child(9) {
          transform: rotate(240deg);
          animation-delay: -0.3s;
        }
        .lds-spinner div:nth-child(10) {
          transform: rotate(270deg);
          animation-delay: -0.2s;
        }
        .lds-spinner div:nth-child(11) {
          transform: rotate(300deg);
          animation-delay: -0.1s;
        }
        .lds-spinner div:nth-child(12) {
          transform: rotate(330deg);
          animation-delay: 0s;
        }
        @keyframes lds-spinner {
          0% {
            opacity: 1;
          }
          100% {
            opacity: 0;
          }
        }
        `;
    document.head.appendChild(style);
  }

  function addAnimationNode(node) {
    if (node === undefined) {
      return undefined;
    }
    const loadingAnimation = document.createElement('div');
    loadingAnimation.className = 'lds-spinner';
    loadingAnimation.innerHTML = `
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>
        <div></div>`;
    node.replaceWith(loadingAnimation);
    return loadingAnimation;
  }

  const observer = new MutationObserver(handleDocumentMutation);
  const options = {
    childList: true,
    attributes: true,
    characterData: true,
    subtree: true,
    attributeFilter: ['type'],
    attributeOldValue: false,
    characterDataOldValue: false
  };

  const htmlNode = document.querySelector('html');

  addAnimationStyle();
  observer.observe(htmlNode, options);
})();
