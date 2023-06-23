import {Quill} from "@vueup/vue-quill";

const Inline = Quill.import('blots/inline');
const BlockEmbed = Quill.import('blots/block/embed');

export class Rt extends Inline {
    static blotName = "rt"
    static tagName = "rt"

    static formats() {
        return true;
    }

}

export class Ruby extends BlockEmbed {
    static blotName = "ruby"
    static tagName = "ruby"
    static allowedChildren = [Rt]
    static scope = 6

    static create(value) {
        // console.log('static create', value)
        const ruby = super.create('ruby');
        if (Array.isArray(value)) {
            let html = ''
            for (let kv of value) {
                html += `${kv.k}<rt>${kv.v}</rt>`
            }
            ruby.innerHTML = html
        } else {
            ruby.innerHTML = '本体<rt>注解</rt>'
        }
        return ruby;
    }

    static formats(domNode) {
        let html = domNode.innerHTML;
        let re = /(?<k>[^<]*)<rt>(?<v>[^<]*)<\/rt>/g;
        let arr = [];
        let result = re.exec(html);
        while (result) {
            arr.push({
                k: result.groups.k,
                v: result.groups.v
            })
            result = re.exec(html)
        }
        // console.log('static formats', arr)
        return arr
    }

    static value(domNode) {
        let html = domNode.innerHTML;
        let re = /(?<k>[^<]*)<rt>(?<v>[^<]*)<\/rt>/g;
        let arr = [];
        let result = re.exec(html);
        while (result) {
            arr.push({
                k: result.groups.k,
                v: result.groups.v
            })
            result = re.exec(html)
        }
        // console.log('static value', arr)
        return arr
    }

    value() {
        let html = this.domNode.innerHTML;
        let re = /(?<k>[^<]*)<rt>(?<v>[^<]*)<\/rt>/g;
        let arr = [];
        let result = re.exec(html);
        while (result) {
            arr.push({
                k: result.groups.k,
                v: result.groups.v
            })
            result = re.exec(html)
        }
        // console.log('value', arr)
        return arr
    }

}
