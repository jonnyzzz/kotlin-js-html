(() => {
  const BASE_URL = 'https://kotlin-html.sandbox.intellij.net';

  function isKotlinScriptNode(node) {
    if (node === undefined) {
      return false;
    }
    return node.tagName === 'SCRIPT' &&
      (node.type === 'language/kotlin' || node.type === 'application/kotlin');
  }

  function getScriptWithNode(node) {
    if (!isKotlinScriptNode(node)) {
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

  function handleDocumentMutation(mutations) {
    for (const mutation of mutations) {
      const scriptWithNode = getKotlinScriptWithNode(mutation);
      if (scriptWithNode === undefined) {
        continue;
      }

      const loadingNode = addAnimationNode(scriptWithNode.node);
      postForCompiledScript(scriptWithNode.script, loadingNode);
    }
  }

  function addJBMonoFont() {
    const linkGApis = document.createElement('link');
    linkGApis.setAttribute('rel', 'preconnect');
    linkGApis.setAttribute('href', 'https://fonts.googleapis.com');
    document.head.appendChild(linkGApis);

    const linkGStatic = document.createElement('link');
    linkGStatic.setAttribute('rel', 'preconnect');
    linkGStatic.setAttribute('href', 'https://fonts.gstatic.com');
    linkGStatic.setAttribute('crossorigin', '');
    document.head.appendChild(linkGStatic);

    const stylesheet = document.createElement('link');
    stylesheet.setAttribute('rel', 'stylesheet');
    stylesheet.setAttribute('href', 'https://fonts.googleapis.com/css2?family=JetBrains+Mono&display=swap');
    document.head.appendChild(stylesheet);
  }

  function addAnimationStyle() {
    const style = document.createElement('style');
    style.innerHTML = `
        .lds-ellipsis {
            display: inline-block;
            position: relative;
            width: 80px;
            height: 40px;
        }
        .lds-ellipsis div {
            position: absolute;
            top: 14px;
            width: 13px;
            height: 12px;
            border-radius: 50%;
            background: #fff;
            animation-timing-function: cubic-bezier(0, 1, 1, 0);
        }
        .lds-ellipsis div:nth-child(1) {
            left: 8px;
            animation: lds-ellipsis1 0.6s infinite;
        }
        .lds-ellipsis div:nth-child(2) {
            left: 8px;
            animation: lds-ellipsis2 0.6s infinite;
        }
        .lds-ellipsis div:nth-child(3) {
            left: 32px;
            animation: lds-ellipsis2 0.6s infinite;
        }
        .lds-ellipsis div:nth-child(4) {
            left: 56px;
            animation: lds-ellipsis3 0.6s infinite;
        }
        @keyframes lds-ellipsis1 {
            0% {
                transform: scale(0);
            }
            100% {
                transform: scale(1);
            }
        }
        @keyframes lds-ellipsis3 {
            0% {
                transform: scale(1);
            }
            100% {
                transform: scale(0);
            }
        }
        @keyframes lds-ellipsis2 {
            0% {
                transform: translate(0, 0);
            }
            100% {
                transform: translate(24px, 0);
            }
        }
        #kjs-background {
            display: inline-flex;
            flex-direction: row;
            flex-wrap: wrap;
            justify-content: center;
            align-items: center;
            background-color: black;
            border: white;
            width: auto;
            position: fixed;
            right: 1em;
            bottom: 1em;
        }
        #kjs-text {
            font-family: 'JetBrains Mono', monospace;
            color: white;
            margin-right: 8px;
            margin-left: 12px;
            margin-top: 4px;
            margin-bottom: 4px;
        }
        #kjs-log {
            height: 256px;
            overflow-y: auto;
            margin-left: 0.6em;
            margin-right: 0.6em;
        }
        #kjs-close {
            font-family: 'JetBrains Mono', monospace;
            color: white;
            font-size: 1em;
            position: fixed;
            right: 1em;
            bottom: 248px;
            padding-right: 12px;
            font-size: 0.8em;
            cursor: pointer;
        }
        .kjs-log-line {
            font-family: 'JetBrains Mono', monospace;
            color: white;
            margin-right: 8px;
            margin-left: 12px;
            margin-top: 4px;
            margin-bottom: 4px;
            font-size: 0.8em;
        }
        `;
    document.head.appendChild(style);
  }

  function prepend(value, array) {
    const newArray = array.slice();
    newArray.unshift(value);
    return newArray;
  }

  function div(id) {
    const result = document.createElement('div');
    result.setAttribute('id', id);
    return result;
  }

  function divClass(c) {
    const result = document.createElement('div');
    result.setAttribute('class', c);
    return result;
  }

  function infoTextDiv(text) {
    const loadingBackground = div('kjs-background');
    const loadingText = div('kjs-text');
    loadingText.textContent = text;
    loadingBackground.appendChild(loadingText);
    return loadingBackground;
  }

  function infoLogDiv(textLinesArray) {
    const background = div('kjs-background');
    const log = div('kjs-log');
    textLinesArray.forEach((txt) => {
      const node = divClass('kjs-log-line');
      node.textContent = txt;
      log.appendChild(node);
    })
    background.appendChild(log);

    const close = div('kjs-close');
    close.textContent = '[X]'
    close.addEventListener('click', () => background.remove())
    background.appendChild(close);
    return background;
  }

  function addAnimationNode(node) {
    if (node === undefined) {
      return undefined;
    }
    const loadingBackground = infoTextDiv('Loading');

    const loadingAnimation = document.createElement('div');
    loadingAnimation.className = 'lds-ellipsis';
    loadingAnimation.innerHTML = '<div></div>'.repeat(4);


    loadingBackground.appendChild(loadingAnimation);
    node.replaceWith(loadingBackground);
    return loadingBackground;
  }

  function fileUrlToScriptNode(scriptSource) {
    if (scriptSource === undefined) {
      return undefined;
    }
    const node = document.createElement('script');
    node.setAttribute('type', 'application/javascript');
    node.setAttribute('src', scriptSource);
    return node;
  }

  function injectJsScriptNodes(scriptNodes, loadingNode) {
    if (loadingNode === undefined) {
      return;
    }
    if (scriptNodes === undefined) {
      return;
    }
    if (!Array.isArray(scriptNodes)) {
      return;
    }
    const parentNode = loadingNode.parentNode;
    scriptNodes.forEach((node) => parentNode.insertBefore(node, loadingNode));
    parentNode.removeChild(loadingNode);
  }

  function fileToJsResultFileUrl(file) {
    if (file === undefined) {
      return undefined;
    }
    if ('string' !== typeof file.cnd_url) {
      return undefined;
    }
    if (!file.cnd_url.endsWith('.js')) {
      return undefined;
    }
    return file.cnd_url;
  }

  function createJsonResponseHandler(script, node) {
    return (response) => {
      if (response === undefined) {
        return;
      }
      if (response.type === 'retry-after-timeout') {
        setTimeout(() => postForCompiledScript(script, node), response.timeout_millis);
        return;
      }
      if (response.type === 'final') {
        if (response.reason !== 'success') {
          throw prepend('Compilation finished with failure:', response.log_output);
        }
        if (!Array.isArray(response.files)) {
          throw 'Invalid data received - should receive array of result files';
        }
        const scriptNodes = response.files
          .map(fileToJsResultFileUrl)
          .map(fileUrlToScriptNode)
          .filter((node) => node !== undefined);
        injectJsScriptNodes(scriptNodes, node);
        return;
      }
      throw `Undefined response result type: ${response.type}`;
    };
  }

  function handleResponseError(node) {
    return (response) => node.replaceWith(infoTextDiv(response));
  }

  function handleJsonError(node) {
    return (error) => {
      if (Array.isArray(error)) {
        node.replaceWith(infoLogDiv(error));
        return;
      }
      node.replaceWith(infoTextDiv(String(error)));
    };
  }

  function createResponseHandler(script, node) {
    return (response) => {
      response.json()
        .then(createJsonResponseHandler(script, node))
        .catch(handleJsonError(node));
    };
  }

  function postForCompiledScript(script, node) {
    if (script === undefined) {
      return;
    }

    fetch(`${BASE_URL}/reception`, {
      method: 'POST',
      body: script
    })
      .then(createResponseHandler(script, node))
      .catch(handleResponseError(node));
  }

  const observer = new MutationObserver(handleDocumentMutation);
  const observerOptions = {
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
  addJBMonoFont();
  observer.observe(htmlNode, observerOptions);
})();
