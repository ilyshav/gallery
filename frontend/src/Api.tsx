export class Api {
    static buildPath(method: string): string {
        return location.protocol + '//' + location.host + '/api' + method
    }

    static buildStaticPath(method: string): string {
        return location.protocol + '//' + location.host + method
    }
}